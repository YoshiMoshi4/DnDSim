package Objects;

public abstract class GridObject {

    protected int row;
    protected int col;

    public GridObject(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
