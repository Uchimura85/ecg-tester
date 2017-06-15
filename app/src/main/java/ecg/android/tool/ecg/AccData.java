package ecg.android.tool.ecg;

/**
 * Created by admin on 4/22/2017.
 */

public class AccData {
    public int e;
    public int x;
    public int y;
    public int z;

    public AccData() {
        this(0, 0, 0,0);
    }

    public AccData(int e,int x, int y, int z) {
        this.e = e;
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
