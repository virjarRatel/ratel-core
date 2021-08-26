package android.graphics.drawable;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.util.AttributeSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
/**
 * Instantiates a drawable XML file into its corresponding
 * {@link android.graphics.drawable.Drawable} objects.
 * <p>
 * For performance reasons, inflation relies heavily on pre-processing of
 * XML files that is done at build time. Therefore, it is not currently possible
 * to use this inflater with an XmlPullParser over a plain XML file at runtime;
 * it only works with an XmlPullParser returned from a compiled resource (R.
 * <em>something</em> file.)
 *
 * @hide Pending API finalization.
 */
public final class DrawableInflater {
    /**
     * Loads the drawable resource with the specified identifier.
     *
     * @param context the context in which the drawable should be loaded
     * @param id the identifier of the drawable resource
     * @return a drawable, or {@code null} if the drawable failed to load
     */
    @Nullable
    public static Drawable loadDrawable(@NonNull Context context, int id) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Loads the drawable resource with the specified identifier.
     *
     * @param resources the resources from which the drawable should be loaded
     * @param theme the theme against which the drawable should be inflated
     * @param id the identifier of the drawable resource
     * @return a drawable, or {@code null} if the drawable failed to load
     */
    @Nullable
    public static Drawable loadDrawable(
            @NonNull Resources resources, @Nullable Theme theme, int id) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Constructs a new drawable inflater using the specified resources and
     * class loader.
     *
     * @param res the resources used to resolve resource identifiers
     * @param classLoader the class loader used to load custom drawables
     * @hide
     */
    public DrawableInflater(@NonNull Resources res, @NonNull ClassLoader classLoader) {
        throw new UnsupportedOperationException("STUB");
    }
    /**
     * Inflates a drawable from inside an XML document using an optional
     * {@link Theme}.
     * <p>
     * This method should be called on a parser positioned at a tag in an XML
     * document defining a drawable resource. It will attempt to create a
     * Drawable from the tag at the current position.
     *
     * @param name the name of the tag at the current position
     * @param parser an XML parser positioned at the drawable tag
     * @param attrs an attribute set that wraps the parser
     * @param theme the theme against which the drawable should be inflated, or
     *              {@code null} to not inflate against a theme
     * @return a drawable
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    @NonNull
    public Drawable inflateFromXml(@NonNull String name, @NonNull XmlPullParser parser,
                                   @NonNull AttributeSet attrs, @Nullable Theme theme)
            throws XmlPullParserException, IOException {
        throw new UnsupportedOperationException("STUB");
    }
}