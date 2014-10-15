package forge.trackable;

import forge.card.CardRarity;
import forge.game.Direction;
import forge.game.GameType;
import forge.game.phase.PhaseType;
import forge.game.zone.ZoneType;
import forge.trackable.TrackableTypes;
import forge.trackable.TrackableTypes.TrackableType;

public enum TrackableProperty {
    //Shared
    Text(TrackableTypes.StringType),
    PreventNextDamage(TrackableTypes.IntegerType),
    EnchantedBy(TrackableTypes.CardViewCollectionType),

    //Card 
    Owner(TrackableTypes.PlayerViewType),
    Controller(TrackableTypes.PlayerViewType),
    Zone(TrackableTypes.EnumType(ZoneType.class)),
    Cloned(TrackableTypes.BooleanType),
    FlipCard(TrackableTypes.BooleanType),
    SplitCard(TrackableTypes.BooleanType),
    RulesText(TrackableTypes.StringType),
    Attacking(TrackableTypes.BooleanType),
    Blocking(TrackableTypes.BooleanType),
    PhasedOut(TrackableTypes.BooleanType),
    Sickness(TrackableTypes.BooleanType),
    Tapped(TrackableTypes.BooleanType),
    Token(TrackableTypes.BooleanType),
    IsCommander(TrackableTypes.BooleanType),
    Counters(TrackableTypes.CounterMapType),
    Damage(TrackableTypes.IntegerType),
    AssignedDamage(TrackableTypes.IntegerType),
    ShieldCount(TrackableTypes.IntegerType),
    ChosenType(TrackableTypes.StringType),
    ChosenColors(TrackableTypes.CardViewCollectionType),
    ChosenPlayer(TrackableTypes.PlayerViewType),
    ChosenDirection(TrackableTypes.EnumType(Direction.class)),
    Remembered(TrackableTypes.StringType),
    NamedCard(TrackableTypes.StringType),
    PlayerMayLook(TrackableTypes.PlayerViewCollectionType),
    PlayerMayLookTemp(TrackableTypes.PlayerViewCollectionType),
    Equipping(TrackableTypes.CardViewType),
    EquippedBy(TrackableTypes.CardViewCollectionType),
    Enchanting(TrackableTypes.GameEntityViewType),
    Fortifying(TrackableTypes.CardViewType),
    FortifiedBy(TrackableTypes.CardViewCollectionType),
    GainControlTargets(TrackableTypes.CardViewCollectionType),
    CloneOrigin(TrackableTypes.CardViewType),
    Cloner(TrackableTypes.StringType),
    ImprintedCards(TrackableTypes.CardViewCollectionType),
    HauntedBy(TrackableTypes.CardViewCollectionType),
    Haunting(TrackableTypes.CardViewType),
    MustBlockCards(TrackableTypes.CardViewCollectionType),
    PairedWith(TrackableTypes.CardViewType),
    CurrentState(TrackableTypes.CardStateViewType, false), //can't respect freeze, otherwise card constructor can crash
    AlternateState(TrackableTypes.CardStateViewType),

    //Card State
    Name(TrackableTypes.StringType),
    Colors(TrackableTypes.ColorSetType),
    ImageKey(TrackableTypes.StringType),
    Type(TrackableTypes.CardTypeViewType),
    ManaCost(TrackableTypes.ManaCostType),
    OracleText(TrackableTypes.StringType),
    SetCode(TrackableTypes.StringType),
    Rarity(TrackableTypes.EnumType(CardRarity.class)),
    Power(TrackableTypes.IntegerType),
    Toughness(TrackableTypes.IntegerType),
    Loyalty(TrackableTypes.IntegerType),
    ChangedColorWords(TrackableTypes.StringMapType),
    ChangedTypes(TrackableTypes.StringMapType),
    HasDeathtouch(TrackableTypes.BooleanType),
    HasHaste(TrackableTypes.BooleanType),
    HasInfect(TrackableTypes.BooleanType),
    HasStorm(TrackableTypes.BooleanType),
    HasTrample(TrackableTypes.BooleanType),
    YouMayLook(TrackableTypes.BooleanType),
    OpponentMayLook(TrackableTypes.BooleanType),
    BlockAdditional(TrackableTypes.IntegerType),
    AbilityText(TrackableTypes.StringType),
    NonAbilityText(TrackableTypes.StringType),
    FoilIndex(TrackableTypes.IntegerType),

    //Player
    AvatarIndex(TrackableTypes.IntegerType),
    Opponents(TrackableTypes.PlayerViewCollectionType),
    Life(TrackableTypes.IntegerType),
    PoisonCounters(TrackableTypes.IntegerType),
    MaxHandSize(TrackableTypes.IntegerType),
    HasUnlimitedHandSize(TrackableTypes.BooleanType),
    NumDrawnThisTurn(TrackableTypes.IntegerType),
    Keywords(TrackableTypes.KeywordCollectionViewType),
    Commander(TrackableTypes.CardViewType),
    CommanderDamage(TrackableTypes.IntegerMapType),
    MindSlaveMaster(TrackableTypes.PlayerViewType),
    Ante(TrackableTypes.CardViewCollectionType),
    Battlefield(TrackableTypes.CardViewCollectionType),
    Command(TrackableTypes.CardViewCollectionType),
    Exile(TrackableTypes.CardViewCollectionType),
    Flashback(TrackableTypes.CardViewCollectionType),
    Graveyard(TrackableTypes.CardViewCollectionType),
    Hand(TrackableTypes.CardViewCollectionType),
    Library(TrackableTypes.CardViewCollectionType),
    Mana(TrackableTypes.ManaMapType),

    //SpellAbility
    HostCard(TrackableTypes.CardViewType),
    Description(TrackableTypes.StringType),
    CanPlay(TrackableTypes.BooleanType),
    PromptIfOnlyPossibleAbility(TrackableTypes.BooleanType),

    //StackItem
    Key(TrackableTypes.StringType),
    SourceTrigger(TrackableTypes.IntegerType),
    SourceCard(TrackableTypes.CardViewType),
    ActivatingPlayer(TrackableTypes.PlayerViewType),
    TargetCards(TrackableTypes.CardViewCollectionType),
    TargetPlayers(TrackableTypes.PlayerViewCollectionType),
    SubInstance(TrackableTypes.StackItemViewType),
    Ability(TrackableTypes.BooleanType),
    OptionalTrigger(TrackableTypes.BooleanType),

    //Combat
    AttackersWithDefenders(TrackableTypes.CardViewCollectionType, false), //TODO: change out for proper types when serialization needed
    AttackersWithBlockers(TrackableTypes.CardViewCollectionType, false),
    BandsWithDefenders(TrackableTypes.CardViewCollectionType, false),
    BandsWithBlockers(TrackableTypes.CardViewCollectionType, false),
    AttackersWithPlannedBlockers(TrackableTypes.CardViewCollectionType, false),
    BandsWithPlannedBlockers(TrackableTypes.CardViewCollectionType, false),

    //Game
    GameType(TrackableTypes.EnumType(GameType.class)),
    Turn(TrackableTypes.IntegerType),
    WinningTeam(TrackableTypes.IntegerType),
    MatchOver(TrackableTypes.BooleanType),
    NumGamesInMatch(TrackableTypes.IntegerType),
    NumPlayedGamesInMatch(TrackableTypes.IntegerType),
    StormCount(TrackableTypes.IntegerType),
    GameOver(TrackableTypes.BooleanType),
    PoisonCountersToLose(TrackableTypes.IntegerType),
    GameLog(TrackableTypes.PlayerViewType),
    PlayerTurn(TrackableTypes.PlayerViewType),
    Phase(TrackableTypes.EnumType(PhaseType.class));

    private final TrackableType<?> type;
    private final boolean respectFreeze;

    private TrackableProperty(TrackableType<?> type0) {
        this(type0, true);
    }
    private TrackableProperty(TrackableType<?> type0, boolean respectFreeze0) {
        type = type0;
        respectFreeze = respectFreeze0;
    }

    public boolean respectFreeze() {
        return respectFreeze;
    }

    @SuppressWarnings("unchecked")
    public <T> T getDefaultValue() {
        return ((TrackableType<T>)type).getDefaultValue();
    }
    @SuppressWarnings("unchecked")
    public <T> T deserialize(TrackableDeserializer td, T oldValue) {
        return ((TrackableType<T>)type).deserialize(td, oldValue);
    }
    @SuppressWarnings("unchecked")
    public <T> void serialize(TrackableSerializer ts, T value) {
        ((TrackableType<T>)type).serialize(ts, value);
    }

    //cache array of all properties to allow quick lookup by ordinal,
    //which reduces the size and improves performance of serialization
    //we don't need to worry about the values changing since we will ensure
    //both players are on the same version of Forge before allowing them to connect
    private static TrackableProperty[] props = values();
    public static int serialize(TrackableProperty prop) {
        return prop.ordinal();
    }
    public static TrackableProperty deserialize(int ordinal) {
        return props[ordinal];
    }
}