package forge.game.combat;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import forge.card.MagicColor;
import forge.game.Game;
import forge.game.GameEntity;
import forge.game.card.Card;
import forge.game.card.CardCollection;
import forge.game.card.CardCollectionView;
import forge.game.card.CardLists;
import forge.game.card.CardPredicates;
import forge.game.card.CounterType;
import forge.game.zone.ZoneType;
import forge.util.FCollection;
import forge.util.FCollectionView;
import forge.util.maps.LinkedHashMapToAmount;
import forge.util.maps.MapToAmount;
import forge.util.maps.MapToAmountUtil;

public class AttackConstraints {

    private final CardCollection possibleAttackers;
    private final FCollectionView<GameEntity> possibleDefenders;
    private final GlobalAttackRestrictions globalRestrictions;

    private final Map<Card, AttackRestriction> restrictions = Maps.newHashMap();
    private final Map<Card, AttackRequirement> requirements = Maps.newHashMap();

    public AttackConstraints(final Combat combat) {
        final Game game = combat.getAttackingPlayer().getGame();
        possibleAttackers = CardLists.filter(combat.getAttackingPlayer().getCardsIn(ZoneType.Battlefield), CardPredicates.Presets.CREATURES);
        possibleDefenders = combat.getDefenders();
        globalRestrictions = GlobalAttackRestrictions.getGlobalRestrictions(combat.getAttackingPlayer(), possibleDefenders);

        // Number of "must attack" constraints on each creature with a magnet counter (equal to the number of permanents requiring that constraint).
        int nMagnetRequirements = 0;
        final CardCollectionView magnetAttackers = CardLists.filter(combat.getAttackers(), CardPredicates.hasCounter(CounterType.MAGNET));
        // Only require if a creature with a magnet counter on it attacks.
        if (!magnetAttackers.isEmpty()) {
            nMagnetRequirements = CardLists.getAmountOfKeyword(
                    game.getCardsIn(ZoneType.Battlefield),
                    "If a creature with a magnet counter on it attacks, all creatures with magnet counters on them attack if able.");
        }

        final MapToAmount<Card> attacksIfOtherAttacks = new LinkedHashMapToAmount<Card>();
        for (final Card possibleAttacker : possibleAttackers) {
            attacksIfOtherAttacks.add(possibleAttacker, possibleAttacker.getAmountOfKeyword("If a creature you control attacks, CARDNAME also attacks if able."));
        }

        for (final Card possibleAttacker : possibleAttackers) {
            restrictions.put(possibleAttacker, new AttackRestriction(possibleAttacker, possibleDefenders));

            final MapToAmount<Card> causesToAttack = new LinkedHashMapToAmount<Card>();
            for (final Entry<Card, Integer> entry : attacksIfOtherAttacks.entrySet()) {
                if (entry.getKey() != possibleAttacker) {
                    causesToAttack.add(entry.getKey(), entry.getValue().intValue());
                }
            }

            // Number of "all must attack" requirements on this attacker
            final int nAllMustAttack = possibleAttacker.getAmountOfKeyword("If CARDNAME attacks, all creatures you control attack if able.");
            for (final Card c : possibleAttackers) {
                if (c != possibleAttacker) {
                    causesToAttack.add(c, nAllMustAttack);
                }
            }

            if (possibleAttacker.getCounters(CounterType.MAGNET) > 0) {
                for (final Card c : magnetAttackers) {
                    if (c != possibleAttacker) {
                        causesToAttack.add(c, nMagnetRequirements);
                    }
                }
            }

            final AttackRequirement r = new AttackRequirement(possibleAttacker, causesToAttack, possibleDefenders);
            requirements.put(possibleAttacker, r);
        }
    }

    /**
     * Get a set of legal attackers.
     * 
     * @return a {@link Pair} of
     *         <ul>
     *         <li>A {@link Map} mapping attacking creatures to defenders;</li>
     *         <li>The number of requirements fulfilled by this attack.</li>
     *         </ul>
     */
    public Pair<Map<Card, GameEntity>, Integer> getLegalAttackers() {
        final int globalMax = globalRestrictions.getMax();
        final int myMax = Ints.min(globalMax == -1 ? Integer.MAX_VALUE : globalMax, possibleAttackers.size());
        if (myMax == 0) {
            return Pair.of(Collections.<Card, GameEntity>emptyMap(), Integer.valueOf(0));
        }

        final MapToAmount<Map<Card, GameEntity>> possible = new LinkedHashMapToAmount<Map<Card, GameEntity>>();
        final List<Attack> reqs = getSortedFilteredRequirements();
        final CardCollection myPossibleAttackers = new CardCollection(possibleAttackers);

        // First, remove all requirements of creatures that aren't going attack this combat anyway
        final CardCollection attackersToRemove = new CardCollection();
        for (final Card attacker : myPossibleAttackers) {
            final Set<AttackRestrictionType> types = restrictions.get(attacker).getTypes();
            if ((types.contains(AttackRestrictionType.NEED_TWO_OTHERS)     && myMax <= 2
                    ) || (
                 types.contains(AttackRestrictionType.NOT_ALONE)           && myMax <= 1
                    ) || (
                 types.contains(AttackRestrictionType.NEED_BLACK_OR_GREEN) && myMax <= 1
                    ) || (
                 types.contains(AttackRestrictionType.NEED_GREATER_POWER)  && myMax <= 1
                            )) {
                reqs.removeAll(findAll(reqs, attacker));
                attackersToRemove.add(attacker);
            }
        }
        myPossibleAttackers.removeAll((Iterable<Card>) attackersToRemove);
        for (final Card toRemove : attackersToRemove) {
            reqs.removeAll(findAll(reqs, toRemove));
        }
        attackersToRemove.clear();

        // Next, remove creatures with constraints that can't be fulfilled.
        for (final Card attacker : myPossibleAttackers) {
            final Set<AttackRestrictionType> types = restrictions.get(attacker).getTypes();
            if (types.contains(AttackRestrictionType.NEED_BLACK_OR_GREEN)) {
                // It's insufficient if this attacker is B/G itself, so filter itself out!
                if (CardLists.filter(myPossibleAttackers, CardPredicates.isColor((byte) (MagicColor.BLACK | MagicColor.GREEN)), Predicates.not(Predicates.equalTo(attacker))).size() == 0) {
                    attackersToRemove.add(attacker);
                }
            } else if (types.contains(AttackRestrictionType.NEED_GREATER_POWER)) {
                if (CardLists.filter(myPossibleAttackers, CardPredicates.hasGreaterPowerThan(attacker.getNetPower())).size() == 0) {
                    attackersToRemove.add(attacker);
                }
            }
        }
        myPossibleAttackers.removeAll((Iterable<Card>) attackersToRemove);
        for (final Card toRemove : attackersToRemove) {
            reqs.removeAll(findAll(reqs, toRemove));
        }

        // First, successively try each creature that must attack alone.
        for (final Card attacker : myPossibleAttackers) {
            if (restrictions.get(attacker).getTypes().contains(AttackRestrictionType.ONLY_ALONE)) {
                final Attack attack = findFirst(reqs, attacker);
                if (attack == null) {
                    // no requirements, we don't care anymore
                    continue;
                }
                final Map<Card, GameEntity> attackMap = ImmutableMap.of(attack.attacker, attack.defender);
                final int violations = countViolations(attackMap);
                if (violations != -1) {
                    possible.put(attackMap, violations);
                }
                // remove them from the requirements, as they'll not be relevant to this calculation any more
                reqs.removeAll(findAll(reqs, attacker));
            }
        }

        // Now try all others (plus empty attack) and count their violations
        final FCollection<Map<Card, GameEntity>> legalAttackers = collectLegalAttackers(reqs, myMax);
        possible.putAll(Maps.asMap(legalAttackers, FN_COUNT_VIOLATIONS));
        possible.put(Collections.<Card, GameEntity>emptyMap(), countViolations(Collections.<Card, GameEntity>emptyMap()));
 
        // take the case with the fewest violations
        return MapToAmountUtil.min(possible);
    }

    private final FCollection<Map<Card, GameEntity>> collectLegalAttackers(final List<Attack> reqs, final int maximum) {
        return new FCollection<Map<Card, GameEntity>>
            (collectLegalAttackers(Collections.<Card, GameEntity>emptyMap(), deepClone(reqs), new CardCollection(), maximum));
    }

    private final List<Map<Card, GameEntity>> collectLegalAttackers(final Map<Card, GameEntity> attackers, final List<Attack> reqs, final CardCollection reserved, final int maximum) {
        final List<Map<Card, GameEntity>> result = Lists.newLinkedList();

        int localMaximum = maximum;
        final boolean isLimited = globalRestrictions.getMax() != -1;
        final Map<Card, GameEntity> myAttackers = Maps.newHashMap(attackers);
        final MapToAmount<GameEntity> toDefender = new LinkedHashMapToAmount<GameEntity>();
        int attackersNeeded = 0;

        outer: while(!reqs.isEmpty()) {
            final Iterator<Attack> iterator = reqs.iterator();
            final Attack req = iterator.next();
            final boolean isReserved = reserved.contains(req.attacker);

            boolean skip = false;
            if (!isReserved) {
                if (localMaximum <= 0) {
                    // can't add any more creatures (except reserved creatures)
                    skip = true;
                } else if (req.requirements == 0 && attackersNeeded == 0 && reserved.isEmpty()) {
                    // we don't need this creature
                    skip = true;
                }
            }
            final Integer defMax = globalRestrictions.getDefenderMax().get(req.defender);
            if (defMax != null && toDefender.count(req.defender) >= defMax.intValue()) {
                // too many to this defender already
                skip = true;
            } else if (null != CombatUtil.getAttackCost(req.attacker.getGame(), req.attacker, req.defender)) {
                // has to pay a cost: skip!
                skip = true;
            }

            if (skip) {
                iterator.remove();
                continue;
            }

            boolean haveTriedWithout = false;
            final AttackRestriction restriction = restrictions.get(req.attacker);
            final AttackRequirement requirement = requirements.get(req.attacker);

            // construct the predicate restrictions
            final Collection<Predicate<Card>> predicateRestrictions = Lists.newLinkedList();
            for (final AttackRestrictionType rType : restriction.getTypes()) {
                final Predicate<Card> predicate = rType.getPredicate(req.attacker);
                if (predicate != null) {
                    predicateRestrictions.add(predicate);
                }
            }

            if (!requirement.getCausesToAttack().isEmpty()) {
                final List<Attack> clonedReqs = deepClone(reqs);
                for (final Entry<Card, Integer> causesToAttack : requirement.getCausesToAttack().entrySet()) {
                    for (final Attack a : findAll(reqs, causesToAttack.getKey())) {
                        a.requirements += causesToAttack.getValue().intValue();
                    }
                }
                // if maximum < no of possible attackers, try both with and without this creature
                if (isLimited) {
                    // try without
                    clonedReqs.removeAll(findAll(clonedReqs, req.attacker));
                    final CardCollection clonedReserved = new CardCollection(reserved);
                    result.addAll(collectLegalAttackers(myAttackers, clonedReqs, clonedReserved, localMaximum));
                    haveTriedWithout = true;
                }
            }

            for (final Predicate<Card> predicateRestriction : predicateRestrictions) {
                if (Iterables.any(Sets.union(myAttackers.keySet(), reserved), predicateRestriction)) {
                    // predicate fulfilled already, ignore!
                    continue;
                }
                // otherwise: reserve first creature to match it!
                final Attack match = findFirst(reqs, predicateRestriction);
                if (match == null) {
                    // no match: remove this creature completely
                    reqs.removeAll(findAll(reqs, req.attacker));
                    continue outer;
                }
                // found one! add it to reserve and lower local maximum
                reserved.add(match.attacker);
                localMaximum--;

                // if limited, try both with and without this creature
                if (!haveTriedWithout && isLimited) {
                    // try without
                    final List<Attack> clonedReqs = deepClone(reqs);
                    clonedReqs.removeAll(findAll(clonedReqs, req.attacker));
                    final CardCollection clonedReserved = new CardCollection(reserved);
                    result.addAll(collectLegalAttackers(myAttackers, clonedReqs, clonedReserved, localMaximum));
                    haveTriedWithout = true;
                }
            }

            // finally: add the creature
            myAttackers.put(req.attacker, req.defender);
            toDefender.add(req.defender);
            reqs.removeAll(findAll(reqs, req.attacker));
            reserved.remove(req.attacker);

            // need two other attackers: set that number to the number of attackers we still need (but never < 0)
            if (restrictions.get(req.attacker).getTypes().contains(AttackRestrictionType.NEED_TWO_OTHERS)) {
                attackersNeeded = Ints.max(3 - (myAttackers.size() + reserved.size()), 0);
            }
        }

        // success if we've added everything we want
        if (reserved.isEmpty() && attackersNeeded == 0) {
            result.add(myAttackers);
        }

        return result;
    }

    private final static class Attack implements Comparable<Attack> {
        private final Card attacker;
        private final GameEntity defender;
        private int requirements;
        private Attack(final Attack other) {
            this(other.attacker, other.defender, other.requirements);
        }
        private Attack(final Card attacker, final GameEntity defender, final int requirements) {
            this.attacker = attacker;
            this.defender = defender;
            this.requirements = requirements;
        }
        @Override
        public int compareTo(final Attack other) {
            return Integer.compare(this.requirements, other.requirements);
        }
        @Override
        public String toString() {
            return "[" + requirements + "] " + attacker + " to " + defender; 
        }
    }

    private final List<Attack> getSortedFilteredRequirements() {
        final List<Attack> result = Lists.newArrayList();
        final Map<Card, List<Pair<GameEntity, Integer>>> sortedRequirements = Maps.transformValues(requirements, AttackRequirement.SORT);
        for (final Entry<Card, List<Pair<GameEntity, Integer>>> reqList : sortedRequirements.entrySet()) {
            final AttackRestriction restriction = restrictions.get(reqList.getKey());
            final List<Pair<GameEntity, Integer>> list = reqList.getValue();
            for (int i = 0; i < list.size(); i++) {
                final Pair<GameEntity, Integer> attackReq = list.get(i);
                if (restriction.canAttack(attackReq.getLeft())) {
                    result.add(new Attack(reqList.getKey(), attackReq.getLeft(), attackReq.getRight()));
                }
            }
        }

        Collections.sort(result);
        return Lists.reverse(result);
    }
    private static List<Attack> deepClone(final List<Attack> original) {
        final List<Attack> newList = Lists.newLinkedList();
        for (final Attack attack : original) {
            newList.add(new Attack(attack));
        }
        return newList;
    }
    private static Attack findFirst(final List<Attack> reqs, final Predicate<Card> predicate) {
        for (final Attack req : reqs) {
            if (predicate.apply(req.attacker)) {
                return req;
            }
        }
        return null;
    }
    private static Attack findFirst(final List<Attack> reqs, final Card attacker) {
        return findFirst(reqs, Predicates.equalTo(attacker));
    }
    private static Collection<Attack> findAll(final List<Attack> reqs, final Card attacker) {
        return Collections2.filter(reqs, new Predicate<Attack>() {
            @Override
            public boolean apply(final Attack input) {
                return input.attacker.equals(attacker);
            }
        });
    }

    /**
     * @param attackers
     *            a {@link Map} of each attacking {@link Card} to the
     *            {@link GameEntity} it's attacking.
     * @return the number of requirements violated by this attack, or -1 if a
     *         restriction is violated.
     */
    public final int countViolations(final Map<Card, GameEntity> attackers) {
        if (!globalRestrictions.isLegal(attackers)) {
            return -1;
        }
        for (final Entry<Card, GameEntity> attacker : attackers.entrySet()) {
            final AttackRestriction restriction = restrictions.get(attacker.getKey());
            if (!restriction.canAttack(attacker.getKey(), attackers)) {
                // Violating a restriction!
                return -1;
            }
        }

        int violations = 0;
        for (final Card possibleAttacker : possibleAttackers) {
            final AttackRequirement requirement = requirements.get(possibleAttacker);
            violations += requirement.countViolations(attackers.get(possibleAttacker), attackers);
        }

        return violations;
    }
    private final Function<Map<Card, GameEntity>, Integer> FN_COUNT_VIOLATIONS = new Function<Map<Card,GameEntity>, Integer>() {
        @Override
        public Integer apply(final Map<Card, GameEntity> input) {
            return Integer.valueOf(countViolations(input));
        }
    };
}
