package android.content.res;

import android.content.res.Resources.Theme;
/**
 * Defines an abstract class for the complex color information, like
 * {@link android.content.res.ColorStateList} or {@link android.content.res.GradientColor}
 * @hide
 */
public abstract class ComplexColor {
    private int mChangingConfigurations;
    /**
     * @return {@code true}  if this ComplexColor changes color based on state, {@code false}
     * otherwise.
     */
    public boolean isStateful() { return false; }
    /**
     * @return the default color.
     */
    public abstract int getDefaultColor();
    /**
     * @hide only for resource preloading
     *
     */
    public abstract ConstantState<ComplexColor> getConstantState();
    /**
     * @hide only for resource preloading
     */
    public abstract boolean canApplyTheme();
    /**
     * @hide only for resource preloading
     */
    public abstract ComplexColor obtainForTheme(Theme t);
    /**
     * @hide only for resource preloading
     */
    final void setBaseChangingConfigurations(int changingConfigurations) {
        mChangingConfigurations = changingConfigurations;
    }
    /**
     * Returns a mask of the configuration parameters for which this color
     * may change, requiring that it be re-created.
     *
     * @return a mask of the changing configuration parameters, as defined by
     *         {@link android.content.pm.ActivityInfo}
     *
     * @see android.content.pm.ActivityInfo
     */
    public int getChangingConfigurations() {
        return mChangingConfigurations;
    }
}