package lib;

/**
 *
 * @author Texhnolyze
 */
public class Timer {

    private long stamp = System.currentTimeMillis();
    
    private long dt;
    private boolean paused;
    
    public void reset() {
        stamp = System.currentTimeMillis();
        if (paused) dt = 0;
    }
    
    public long msPassed() {
        return paused ? dt : System.currentTimeMillis() - stamp;
    }
    
    public boolean passed(long ms) {
        return msPassed() >= ms;
    }
    
    public boolean paused() {
        return paused;
    }
    
    public void pause() {
        if (!paused) {
            paused = true;
            dt = System.currentTimeMillis() - stamp;
        }
    }
    
    public void resume() {
        if (paused) {
            paused = false;
            stamp = System.currentTimeMillis() - dt;
        }
    }
    
}
