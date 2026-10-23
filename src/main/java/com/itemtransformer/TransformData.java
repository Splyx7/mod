package com.itemtransformer;

public class TransformData {
    public float scale = 1f;
    public float px = 0f, py = 0f, pz = 0f;
    public float rx = 0f, ry = 0f, rz = 0f;
    public float groundScale = 1f;

    public boolean isDefault() {
        return scale == 1f && px == 0f && py == 0f && pz == 0f
                && rx == 0f && ry == 0f && rz == 0f && groundScale == 1f;
    }

    public void reset() {
        scale = 1f; px = py = pz = 0f; rx = ry = rz = 0f; groundScale = 1f;
    }
}
