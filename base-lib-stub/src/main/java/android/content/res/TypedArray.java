package android.content.res;

import android.annotation.Nullable;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;

/**
 * Container for an array of values that were retrieved with
 * {@link Resources.Theme#obtainStyledAttributes(AttributeSet, int[], int, int)}
 * or {@link Resources#obtainAttributes}.  Be
 * sure to call {@link #recycle} when done with them.
 *
 * The indices used to retrieve values from this structure correspond to
 * the positions of the attributes given to obtainStyledAttributes.
 */
public class TypedArray {
    /**
     * Returns the number of values in this array.
     *
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public int length() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Returns the number of indices in the array that actually have data. Attributes with a value
     * of @empty are included, as this is an explicit indicator.
     *
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public int getIndexCount() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Returns an index in the array that has data. Attributes with a value of @empty are included,
     * as this is an explicit indicator.
     *
     * @param at The index you would like to returned, ranging from 0 to
     *           {@link #getIndexCount()}.
     *
     * @return The index at the given offset, which can be used with
     *         {@link #getValue} and related APIs.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public int getIndex(int at) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Returns the Resources object this array was loaded from.
     *
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public Resources getResources() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieves the styled string value for the attribute at <var>index</var>.
     * <p>
     * If the attribute is not a string, this method will attempt to coerce
     * it to a string.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return CharSequence holding string data. May be styled. Returns
     *         {@code null} if the attribute is not defined or could not be
     *         coerced to a string.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public CharSequence getText(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieves the string value for the attribute at <var>index</var>.
     * <p>
     * If the attribute is not a string, this method will attempt to coerce
     * it to a string.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return String holding string data. Any styling information is removed.
     *         Returns {@code null} if the attribute is not defined or could
     *         not be coerced to a string.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    @Nullable
    public String getString(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieves the string value for the attribute at <var>index</var>, but
     * only if that string comes from an immediate value in an XML file.  That
     * is, this does not allow references to string resources, string
     * attributes, or conversions from other types.  As such, this method
     * will only return strings for TypedArray objects that come from
     * attributes in an XML file.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return String holding string data. Any styling information is removed.
     *         Returns {@code null} if the attribute is not defined or is not
     *         an immediate string value.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public String getNonResourceString(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieves the string value for the attribute at <var>index</var> that is
     * not allowed to change with the given configurations.
     *
     * @param index Index of attribute to retrieve.
     * @param allowedChangingConfigs Bit mask of configurations from
     *        {@link Configuration}.NATIVE_CONFIG_* that are allowed to change.
     *
     * @return String holding string data. Any styling information is removed.
     *         Returns {@code null} if the attribute is not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @hide
     */
    public String getNonConfigurationString(int index,
                                            int allowedChangingConfigs) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the boolean value for the attribute at <var>index</var>.
     * <p>
     * If the attribute is an integer value, this method will return whether
     * it is equal to zero. If the attribute is not a boolean or integer value,
     * this method will attempt to coerce it to an integer using
     * {@link Integer#decode(String)} and return whether it is equal to zero.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 cannot be coerced to an integer.
     *
     * @return Boolean value of the attribute, or defValue if the attribute was
     *         not defined or could not be coerced to an integer.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public boolean getBoolean(int index, boolean defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the integer value for the attribute at <var>index</var>.
     * <p>
     * If the attribute is not an integer, this method will attempt to coerce
     * it to an integer using {@link Integer#decode(String)}.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 cannot be coerced to an integer.
     *
     * @return Integer value of the attribute, or defValue if the attribute was
     *         not defined or could not be coerced to an integer.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public int getInt(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the float value for the attribute at <var>index</var>.
     * <p>
     * If the attribute is not a float or an integer, this method will attempt
     * to coerce it to a float using {@link Float#parseFloat(String)}.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return Attribute float value, or defValue if the attribute was
     *         not defined or could not be coerced to a float.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public float getFloat(int index, float defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the color value for the attribute at <var>index</var>.  If
     * the attribute references a color resource holding a complex
     * {@link android.content.res.ColorStateList}, then the default color from
     * the set is returned.
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not an integer color or color state list.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute color value, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not an integer color or color state list.
     */
    public int getColor(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the ComplexColor for the attribute at <var>index</var>.
     * The value may be either a {@link android.content.res.ColorStateList} which can wrap a simple
     * color value or a {@link android.content.res.GradientColor}
     * <p>
     * This method will return {@code null} if the attribute is not defined or
     * is not an integer color, color state list or GradientColor.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return ComplexColor for the attribute, or {@code null} if not defined.
     * @throws RuntimeException if the attribute if the TypedArray has already
     *         been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not an integer color, color state list or GradientColor.
     * @hide
     */
    @Nullable
    public ComplexColor getComplexColor(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the ColorStateList for the attribute at <var>index</var>.
     * The value may be either a single solid color or a reference to
     * a color or complex {@link android.content.res.ColorStateList}
     * description.
     * <p>
     * This method will return {@code null} if the attribute is not defined or
     * is not an integer color or color state list.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return ColorStateList for the attribute, or {@code null} if not
     *         defined.
     * @throws RuntimeException if the attribute if the TypedArray has already
     *         been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not an integer color or color state list.
     */
    @Nullable
    public ColorStateList getColorStateList(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the integer value for the attribute at <var>index</var>.
     * <p>
     * Unlike {@link #getInt(int, int)}, this method will throw an exception if
     * the attribute is defined but is not an integer.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute integer value, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not an integer.
     */
    public int getInteger(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve a dimensional unit attribute at <var>index</var>. Unit
     * conversions are based on the current {@link DisplayMetrics}
     * associated with the resources this {@link TypedArray} object
     * came from.
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not a dimension.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute dimension value multiplied by the appropriate
     *         metric, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not an integer.
     *
     * @see #getDimensionPixelOffset
     * @see #getDimensionPixelSize
     */
    public float getDimension(int index, float defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve a dimensional unit attribute at <var>index</var> for use
     * as an offset in raw pixels.  This is the same as
     * {@link #getDimension}, except the returned value is converted to
     * integer pixels for you.  An offset conversion involves simply
     * truncating the base value to an integer.
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not a dimension.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute dimension value multiplied by the appropriate
     *         metric and truncated to integer pixels, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not an integer.
     *
     * @see #getDimension
     * @see #getDimensionPixelSize
     */
    public int getDimensionPixelOffset(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve a dimensional unit attribute at <var>index</var> for use
     * as a size in raw pixels.  This is the same as
     * {@link #getDimension}, except the returned value is converted to
     * integer pixels for use as a size.  A size conversion involves
     * rounding the base value, and ensuring that a non-zero base value
     * is at least one pixel in size.
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not a dimension.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute dimension value multiplied by the appropriate
     *         metric and truncated to integer pixels, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not a dimension.
     *
     * @see #getDimension
     * @see #getDimensionPixelOffset
     */
    public int getDimensionPixelSize(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Special version of {@link #getDimensionPixelSize} for retrieving
     * {@link android.view.ViewGroup}'s layout_width and layout_height
     * attributes.  This is only here for performance reasons; applications
     * should use {@link #getDimensionPixelSize}.
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not a dimension or integer (enum).
     *
     * @param index Index of the attribute to retrieve.
     * @param name Textual name of attribute for error reporting.
     *
     * @return Attribute dimension value multiplied by the appropriate
     *         metric and truncated to integer pixels.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not a dimension or integer (enum).
     */
    public int getLayoutDimension(int index, String name) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Special version of {@link #getDimensionPixelSize} for retrieving
     * {@link android.view.ViewGroup}'s layout_width and layout_height
     * attributes.  This is only here for performance reasons; applications
     * should use {@link #getDimensionPixelSize}.
     *
     * @param index Index of the attribute to retrieve.
     * @param defValue The default value to return if this attribute is not
     *                 default or contains the wrong type of data.
     *
     * @return Attribute dimension value multiplied by the appropriate
     *         metric and truncated to integer pixels.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public int getLayoutDimension(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieves a fractional unit attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param base The base value of this fraction.  In other words, a
     *             standard fraction is multiplied by this value.
     * @param pbase The parent base value of this fraction.  In other
     *             words, a parent fraction (nn%p) is multiplied by this
     *             value.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute fractional value multiplied by the appropriate
     *         base value, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not a fraction.
     */
    public float getFraction(int index, int base, int pbase, float defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieves the resource identifier for the attribute at
     * <var>index</var>.  Note that attribute resource as resolved when
     * the overall {@link TypedArray} object is retrieved.  As a
     * result, this function will return the resource identifier of the
     * final resource value that was found, <em>not</em> necessarily the
     * original resource that was specified by the attribute.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or
     *                 not a resource.
     *
     * @return Attribute resource identifier, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public int getResourceId(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieves the theme attribute resource identifier for the attribute at
     * <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param defValue Value to return if the attribute is not defined or not a
     *                 resource.
     *
     * @return Theme attribute resource identifier, or defValue if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @hide
     */
    public int getThemeAttributeId(int index, int defValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the Drawable for the attribute at <var>index</var>.
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not a color or drawable resource.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return Drawable for the attribute, or {@code null} if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not a color or drawable resource.
     */
    @Nullable
    public Drawable getDrawable(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Version of {@link #getDrawable(int)} that accepts an override density.
     * @hide
     */
    @Nullable
    public Drawable getDrawableForDensity(int index, int density) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the Typeface for the attribute at <var>index</var>.
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not a font.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return Typeface for the attribute, or {@code null} if not defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @throws UnsupportedOperationException if the attribute is defined but is
     *         not a font resource.
     */
    @Nullable
    public Typeface getFont(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the CharSequence[] for the attribute at <var>index</var>.
     * This gets the resource ID of the selected attribute, and uses
     * {@link Resources#getTextArray Resources.getTextArray} of the owning
     * Resources object to retrieve its String[].
     * <p>
     * This method will throw an exception if the attribute is defined but is
     * not a text array resource.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return CharSequence[] for the attribute, or {@code null} if not
     *         defined.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public CharSequence[] getTextArray(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the raw TypedValue for the attribute at <var>index</var>.
     *
     * @param index Index of attribute to retrieve.
     * @param outValue TypedValue object in which to place the attribute's
     *                 data.
     *
     * @return {@code true} if the value was retrieved and not @empty, {@code false} otherwise.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public boolean getValue(int index, TypedValue outValue) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Returns the type of attribute at the specified index.
     *
     * @param index Index of attribute whose type to retrieve.
     *
     * @return Attribute type.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public int getType(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Determines whether there is an attribute at <var>index</var>.
     * <p>
     * <strong>Note:</strong> If the attribute was set to {@code @empty} or
     * {@code @undefined}, this method returns {@code false}.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return True if the attribute has a value, false otherwise.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public boolean hasValue(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Determines whether there is an attribute at <var>index</var>, returning
     * {@code true} if the attribute was explicitly set to {@code @empty} and
     * {@code false} only if the attribute was undefined.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return True if the attribute has a value or is empty, false otherwise.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public boolean hasValueOrEmpty(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Retrieve the raw TypedValue for the attribute at <var>index</var>
     * and return a temporary object holding its data.  This object is only
     * valid until the next call on to {@link TypedArray}.
     *
     * @param index Index of attribute to retrieve.
     *
     * @return Returns a TypedValue object if the attribute is defined,
     *         containing its data; otherwise returns null.  (You will not
     *         receive a TypedValue whose type is TYPE_NULL.)
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public TypedValue peekValue(int index) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Returns a message about the parser state suitable for printing error messages.
     *
     * @return Human-readable description of current parser state.
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public String getPositionDescription() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Recycles the TypedArray, to be re-used by a later caller. After calling
     * this function you must not ever touch the typed array again.
     *
     * @throws RuntimeException if the TypedArray has already been recycled.
     */
    public void recycle() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Extracts theme attributes from a typed array for later resolution using
     * {@link android.content.res.Resources.Theme#resolveAttributes(int[], int[])}.
     * Removes the entries from the typed array so that subsequent calls to typed
     * getters will return the default value without crashing.
     *
     * @return an array of length {@link #getIndexCount()} populated with theme
     *         attributes, or null if there are no theme attributes in the typed
     *         array
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @hide
     */
    @Nullable
    public int[] extractThemeAttrs() {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * @hide
     */
    @Nullable
    public int[] extractThemeAttrs(@Nullable int[] scrap) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Return a mask of the configuration parameters for which the values in
     * this typed array may change.
     *
     * @return Returns a mask of the changing configuration parameters, as
     *         defined by {@link android.content.pm.ActivityInfo}.
     * @throws RuntimeException if the TypedArray has already been recycled.
     * @see android.content.pm.ActivityInfo
     */
    public int getChangingConfigurations() {
        throw new UnsupportedOperationException("STUB");
    }
    /** @hide */
    public TypedArray(Resources resources) {
        throw new UnsupportedOperationException("STUB");
    }

    /** Only for API stubs creation, DO NOT USE! */
    /*package*/ TypedArray() {
        throw new UnsupportedOperationException("STUB");
    }
}