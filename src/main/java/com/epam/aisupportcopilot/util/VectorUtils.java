package com.epam.aisupportcopilot.util;

/**
 * Utility for converting embedding vectors to the pgvector string format ({@code [0.1,0.2,...]}).
 */
public final class VectorUtils {

    private VectorUtils() {
    }

    /**
     * Converts a float array embedding into the pgvector-compatible string representation.
     *
     * @param vector embedding array from the embedding model
     * @return string in the format {@code [0.1,0.2,...]} suitable for PostgreSQL cast to vector type
     */
    public static String toVectorString(float[] vector) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < vector.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(vector[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}