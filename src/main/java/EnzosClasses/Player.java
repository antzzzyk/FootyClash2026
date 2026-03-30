package EnzosClasses;

public class Player {
    private Token[] tokens =  new Token[5];
    private int id;
    private int score;
    private boolean turn;

    public Player(Token[] tokens, int id) {
        this.tokens = tokens;
        this.id = id;
        score = 0;
        turn = false;
    }

    public int  getScore() {
        return score;
    }

    public int  getId() {
        return id;
    }

    public void increaseScore(){
        score++;
    }

    public void resetScore(){
        score = 0;
    }
}
