package record;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class Branch {
    private Optional<Commit> optionalTip;

    public Branch(Commit tip) {
        this.optionalTip = Optional.of(tip);
    }

    public Optional<Commit> getTip() {
        return optionalTip;
    }

    public void setTip(Commit tip) {
        this.optionalTip = Optional.of(tip);
    }

    public Optional<byte[]> getBytes() {
        return optionalTip.map(
            tip -> Base16.encode(tip.getBytes()).getBytes(StandardCharsets.UTF_8)
        );
    }
}
