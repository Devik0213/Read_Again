package kim.harry.com.readfav;

/**
 * Created by Naver on 16. 3. 8..
 */
public enum ActionType {
    READ(0), SAVE(1);

    private final int action;

    ActionType(int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }
}
