package forge.game.event;

public class GameEventDrawCard extends GameEvent {
    
    
    @Override
    public <T> T visit(IGameEventVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
