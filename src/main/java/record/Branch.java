package record;

import java.nio.charset.StandardCharsets;

public class Branch {
    private Commit tip;

    public Branch(Commit tip) {
        this.tip = tip;
    }

    public Commit getTip() {
        return tip;
    }

    public void setTip(Commit tip) {
        this.tip = tip;
    }

    public byte[] getBytes() {
        return Base16.encode(tip.getBytes()).getBytes(StandardCharsets.UTF_8);
    }
}
