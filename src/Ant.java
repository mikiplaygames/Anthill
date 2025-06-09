
public final class Ant {
    public static int MaxAge = 50;
    private int age;
    
    public int x;
    public int y;

    public Ant(int x, int y) {
        this.age = 0;
        SetPosition(x, y);
    }
    public boolean CanCopulate() {
        return age >= MaxAge * 0.3f && !IsDead();
    }
    public void IncrementAge() {
        age++;
        if (age >= MaxAge) 
            App.Instance.RemoveAnt(this);
    }
    public int GetAge() {
        return age;
    }
    public boolean IsDead() {
        return age >= MaxAge * 0.8f;
    }
    public void SetPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int GetX() {
        return x;
    }
    public int GetY() {
        return y;
    }
}