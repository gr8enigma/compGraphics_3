public class Position {

    public final int xPos;
    public final int yPos;

    public Position(int xPos, int yPos) {
        this.xPos = xPos;
        this.yPos = yPos;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result + this.xPos;
        result = prime * result + this.yPos;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position))
            return false;
        if (this == obj) return true;
        Position other = (Position) obj;
        return this.xPos == other.xPos && this.yPos == other.yPos;
    }
}

