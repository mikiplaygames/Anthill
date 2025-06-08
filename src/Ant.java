
public class Ant {
    public static int MaxAge = 50;
    private int age;

    public Ant() {
        this.age = 0;
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
}