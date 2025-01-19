package io.github.defective4.sdr.rds;

public class RDSFlags {
    private final boolean ta, tp, stereo, music;

    private RDSFlags(boolean ta, boolean tp, boolean stereo, boolean music) {
        this.ta = ta;
        this.tp = tp;
        this.stereo = stereo;
        this.music = music;
    }

    public boolean hasTA() {
        return ta;
    }

    public boolean hasTP() {
        return tp;
    }

    public boolean isMusic() {
        return music;
    }

    public boolean isStereo() {
        return stereo;
    }

    @Override
    public String toString() {
        return "RDSFlags [ta=" + ta + ", tp=" + tp + ", stereo=" + stereo + ", music=" + music + "]";
    }

    protected static RDSFlags parse(String str) {
        if (str == null || str.isBlank()) return new RDSFlags(false, false, false, false);
        char[] chs = str.toCharArray();
        boolean tp = false;
        boolean ta = false;
        boolean music = false;
        boolean stereo = false;
        for (int i = 0; i < chs.length; i++) if (chs[i] == '1') switch (i) {
            case 0 -> tp = true;
            case 1 -> ta = true;
            case 2 -> music = true;
            case 6 -> stereo = true;
            default -> {}
        }
        return new RDSFlags(ta, tp, stereo, music);
    }

}
