package jarmos.visual;

/**
 * 
 * The color generator is used to produce RGBA (RGB+Alpha) values from a given array of floats.
 * 
 * The resulting array is four times the size of the original array.
 * 
 * For coloring, any given ColorMap instance is used to obtain suitable coloring. If none is given, the
 * #getDefaultColors method can be used.
 * 
 * @note Thus far only the original colormap from @ref rbappmit is included here without using the ColorMap enum.
 * 
 * @author Daniel Wirtz
 * @date Aug 29, 2011
 * 
 */
public class ColorGenerator {

	/**
	 * The color map to use
	 */
	public ColorMap ColorMap;

	/**
	 * default alpha value, use 1.0f for nonblend rendering
	 */
	public float alphaValue = 0.8f;

	/**
	 * Returns an array with "size" default colors (effectively 4*size RGBT values)
	 * 
	 * @param size
	 * @return
	 */
	public float[] getDefaultColor(int size) {
		float[] colors = new float[size * 4];
		for (int i = 0; i < colors.length; i += 4) {
			colors[i] = 0.0f;
			colors[i + 1] = 0.0f;
			colors[i + 2] = 1.0f;
			colors[i + 3] = alphaValue;
		}
		return colors;
	}

	/**
	 * Computes a 4-tuple color array with values R, G, B, Alpha for the given field values.
	 * 
	 * @param fieldValues
	 * @return The color array for the given fieldValues and color map
	 */
	public float[] computeColors(float[] fieldValues) {

		float[] colors = new float[fieldValues.length * 4];
		float min = getMin(fieldValues);
		float max = getMax(fieldValues);

		// calculate color data
		for (int i = 0; i < fieldValues.length; i++) {
			float tmpvar = (fieldValues[i] - min) / (max - min);
			if (tmpvar <= 0.125f) {
				colors[i * 4 + 0] = 0.0f;
				colors[i * 4 + 1] = 0.0f;
				colors[i * 4 + 2] = 0.5f + tmpvar / 0.25f;
				colors[i * 4 + 3] = alphaValue;
			}
			if ((tmpvar > 0.125f) && (tmpvar <= 0.375f)) {
				colors[i * 4 + 0] = 0.0f;
				colors[i * 4 + 1] = 0.0f + (tmpvar - 0.125f) / 0.25f;
				colors[i * 4 + 2] = 1.0f;
				colors[i * 4 + 3] = alphaValue;
			}
			if ((tmpvar > 0.375f) && (tmpvar <= 0.625f)) {
				colors[i * 4 + 0] = 0.0f + (tmpvar - 0.375f) / 0.25f;
				colors[i * 4 + 1] = 1.0f;
				colors[i * 4 + 2] = 1.0f - (tmpvar - 0.375f) / 0.25f;
				colors[i * 4 + 3] = alphaValue;
			}
			if ((tmpvar > 0.625f) && (tmpvar <= 0.875f)) {
				colors[i * 4 + 0] = 1.0f;
				colors[i * 4 + 1] = 1.0f - (tmpvar - 0.625f) / 0.25f;
				colors[i * 4 + 2] = 0.0f;
				colors[i * 4 + 3] = alphaValue;
			}
			if (tmpvar > 0.875f) {
				colors[i * 4 + 0] = 1.0f - (tmpvar - 0.875f) / 0.25f;
				colors[i * 4 + 1] = 0.0f;
				colors[i * 4 + 2] = 0.0f;
				colors[i * 4 + 3] = alphaValue;
			}
		}
		return colors;
	}

	private float getMin(float[] values) {
		float min = values[0];
		for (int j = 0; j < values.length; j++) {
			min = (min > values[j]) ? values[j] : min;
		}
		return min;
	}

	private float getMax(float[] values) {
		float min = values[0];
		for (int j = 0; j < values.length; j++) {
			min = (min < values[j]) ? values[j] : min;
		}
		return min;
	}

}
