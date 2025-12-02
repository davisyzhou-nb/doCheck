/*
 * Copyright 2012 - 2013 Benjamin Weiss
 * Copyright 2012 Neofonie Mobile GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zlian.netgap.component;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public final class Crouton {
	private static final int IMAGE_ID = 0x100;
	private static final int TEXT_ID = 0x101;
	private final CharSequence text;
	private final Style style;
	private Configuration configuration = null;
	private final View customView;
	private OnClickListener onClickListener;
	private Activity activity;
	private ViewGroup viewGroup;
	private FrameLayout croutonView;
	private Animation inAnimation;
	private Animation outAnimation;
	private LifecycleCallback lifecycleCallback = null;

	private Crouton(Activity activity, CharSequence text, Style style) {
		if ((activity == null) || (text == null) || (style == null)) {
			throw new IllegalArgumentException(
					"Null parameters are not accepted");
		}

		this.activity = activity;
		this.viewGroup = null;
		this.text = text;
		this.style = style;
		this.customView = null;
	}

	private Crouton(Activity activity, CharSequence text, Style style,
			ViewGroup viewGroup) {
		if ((activity == null) || (text == null) || (style == null)) {
			throw new IllegalArgumentException(
					"Null parameters are not accepted");
		}

		this.activity = activity;
		this.text = text;
		this.style = style;
		this.viewGroup = viewGroup;
		this.customView = null;
	}

	private Crouton(Activity activity, View customView) {
		if ((activity == null) || (customView == null)) {
			throw new IllegalArgumentException(
					"Null parameters are not accepted");
		}

		this.activity = activity;
		this.viewGroup = null;
		this.customView = customView;
		this.style = new Style.Builder().build();
		this.text = null;
	}

	private Crouton(Activity activity, View customView, ViewGroup viewGroup) {
		this(activity, customView, viewGroup, Configuration.DEFAULT);
	}

	private Crouton(final Activity activity, final View customView,
			final ViewGroup viewGroup, final Configuration configuration) {
		if ((activity == null) || (customView == null)) {
			throw new IllegalArgumentException(
					"Null parameters are not accepted");
		}

		this.activity = activity;
		this.customView = customView;
		this.viewGroup = viewGroup;
		this.style = new Style.Builder().build();
		this.text = null;
		this.configuration = configuration;
	}

	public static Crouton makeText(Activity activity, CharSequence text,
			Style style) {
		return new Crouton(activity, text, style);
	}

	public static Crouton makeText(Activity activity, CharSequence text,
			Style style, ViewGroup viewGroup) {
		return new Crouton(activity, text, style, viewGroup);
	}

	public static Crouton makeText(Activity activity, CharSequence text,
			Style style, int viewGroupResId) {
		return new Crouton(activity, text, style,
				(ViewGroup) activity.findViewById(viewGroupResId));
	}

	public static Crouton makeText(Activity activity, int textResourceId,
			Style style) {
		return makeText(activity, activity.getString(textResourceId), style);
	}

	public static Crouton makeText(Activity activity, int textResourceId,
			Style style, ViewGroup viewGroup) {
		return makeText(activity, activity.getString(textResourceId), style,
				viewGroup);
	}

	public static Crouton makeText(Activity activity, int textResourceId,
			Style style, int viewGroupResId) {
		return makeText(activity, activity.getString(textResourceId), style,
				(ViewGroup) activity.findViewById(viewGroupResId));
	}

	public static Crouton make(Activity activity, View customView) {
		return new Crouton(activity, customView);
	}

	public static Crouton make(Activity activity, View customView,
			ViewGroup viewGroup) {
		return new Crouton(activity, customView, viewGroup);
	}

	public static Crouton make(Activity activity, View customView,
			int viewGroupResId) {
		return new Crouton(activity, customView,
				(ViewGroup) activity.findViewById(viewGroupResId));
	}

	public static Crouton make(Activity activity, View customView,
			int viewGroupResId, final Configuration configuration) {
		return new Crouton(activity, customView,
				(ViewGroup) activity.findViewById(viewGroupResId),
				configuration);
	}

	public static void showText(Activity activity, CharSequence text,
			Style style) {
		makeText(activity, text, style).show();
	}

	public static void showText(Activity activity, CharSequence text,
			Style style, ViewGroup viewGroup) {
		makeText(activity, text, style, viewGroup).show();
	}

	public static void showText(Activity activity, CharSequence text,
			Style style, int viewGroupResId) {
		makeText(activity, text, style,
				(ViewGroup) activity.findViewById(viewGroupResId)).show();
	}

	public static void showText(Activity activity, CharSequence text,
			Style style, int viewGroupResId, final Configuration configuration) {
		makeText(activity, text, style,
				(ViewGroup) activity.findViewById(viewGroupResId))
				.setConfiguration(configuration).show();
	}

	public static void show(Activity activity, View customView) {
		make(activity, customView).show();
	}

	public static void show(Activity activity, View customView,
			ViewGroup viewGroup) {
		make(activity, customView, viewGroup).show();
	}

	public static void show(Activity activity, View customView,
			int viewGroupResId) {
		make(activity, customView, viewGroupResId).show();
	}

	public static void showText(Activity activity, int textResourceId,
			Style style) {
		showText(activity, activity.getString(textResourceId), style);
	}

	public static void showText(Activity activity, int textResourceId,
			Style style, ViewGroup viewGroup) {
		showText(activity, activity.getString(textResourceId), style, viewGroup);
	}

	public static void showText(Activity activity, int textResourceId,
			Style style, int viewGroupResId) {
		showText(activity, activity.getString(textResourceId), style,
				viewGroupResId);
	}

	public static void hide(Crouton crouton) {
		Manager.getInstance().removeCrouton(crouton);
	}

	public static void cancelAllCroutons() {
		Manager.getInstance().clearCroutonQueue();
	}

	public static void clearCroutonsForActivity(Activity activity) {
		Manager.getInstance().clearCroutonsForActivity(activity);
	}

	/** Cancels a {@link Crouton} immediately. */
	public void cancel() {
		Manager manager = Manager.getInstance();
		manager.removeCroutonImmediately(this);
	}

	public void show() {
		Manager.getInstance().add(this);
	}

	public Animation getInAnimation() {
		if ((null == this.inAnimation) && (null != this.activity)) {
			if (getConfiguration().inAnimationResId > 0) {
				this.inAnimation = AnimationUtils.loadAnimation(getActivity(),
						getConfiguration().inAnimationResId);
			} else {
				measureCroutonView();
				this.inAnimation = DefaultAnimationsBuilder
						.buildDefaultSlideInDownAnimation(getView());
			}
		}

		return inAnimation;
	}

	public Animation getOutAnimation() {
		if ((null == this.outAnimation) && (null != this.activity)) {
			if (getConfiguration().outAnimationResId > 0) {
				this.outAnimation = AnimationUtils.loadAnimation(getActivity(),
						getConfiguration().outAnimationResId);
			} else {
				this.outAnimation = DefaultAnimationsBuilder
						.buildDefaultSlideOutUpAnimation(getView());
			}
		}

		return outAnimation;
	}

	public void setLifecycleCallback(LifecycleCallback lifecycleCallback) {
		this.lifecycleCallback = lifecycleCallback;
	}

	public Crouton setOnClickListener(OnClickListener onClickListener) {
		this.onClickListener = onClickListener;
		return this;
	}

	public Crouton setConfiguration(final Configuration configuration) {
		this.configuration = configuration;
		return this;
	}

	@Override
	public String toString() {
		return "Crouton{" + "text=" + text + ", style=" + style
				+ ", configuration=" + configuration + ", customView="
				+ customView + ", onClickListener=" + onClickListener
				+ ", activity=" + activity + ", viewGroup=" + viewGroup
				+ ", croutonView=" + croutonView + ", inAnimation="
				+ inAnimation + ", outAnimation=" + outAnimation
				+ ", lifecycleCallback=" + lifecycleCallback + '}';
	}

	public static String getLicenseText() {
		return "This application uses the Crouton library.\n\n"
				+ "Copyright 2012 - 2013 Benjamin Weiss \n"
				+ "Copyright 2012 Neofonie Mobile GmbH\n"
				+ "\n"
				+ "Licensed under the Apache License, Version 2.0 (the \"License\");\n"
				+ "you may not use this file except in compliance with the License.\n"
				+ "You may obtain a copy of the License at\n"
				+ "\n"
				+ "   http://www.apache.org/licenses/LICENSE-2.0\n"
				+ "\n"
				+ "Unless required by applicable law or agreed to in writing, software\n"
				+ "distributed under the License is distributed on an \"AS IS\" BASIS,\n"
				+ "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
				+ "See the License for the specific language governing permissions and\n"
				+ "limitations under the License.";
	}

	boolean isShowing() {
		return (null != activity)
				&& (isCroutonViewNotNull() || isCustomViewNotNull());
	}

	private boolean isCroutonViewNotNull() {
		return (null != croutonView) && (null != croutonView.getParent());
	}

	private boolean isCustomViewNotNull() {
		return (null != customView) && (null != customView.getParent());
	}

	/** Removes the activity reference this {@link Crouton} is holding */
	void detachActivity() {
		activity = null;
	}

	/** Removes the viewGroup reference this {@link Crouton} is holding */
	void detachViewGroup() {
		viewGroup = null;
	}

	/** Removes the lifecycleCallback reference this {@link Crouton} is holding */
	void detachLifecycleCallback() {
		lifecycleCallback = null;
	}

	/** @return the lifecycleCallback */
	LifecycleCallback getLifecycleCallback() {
		return lifecycleCallback;
	}

	/** @return the style */
	Style getStyle() {
		return style;
	}

	/** @return this croutons configuration */
	Configuration getConfiguration() {
		if (null == configuration) {
			configuration = getStyle().configuration;
		}
		return configuration;
	}

	/** @return the activity */
	Activity getActivity() {
		return activity;
	}

	/** @return the viewGroup */
	ViewGroup getViewGroup() {
		return viewGroup;
	}

	/** @return the text */
	CharSequence getText() {
		return text;
	}

	/** @return the view */
	View getView() {
		// return the custom view if one exists
		if (null != this.customView) {
			return this.customView;
		}

		// if already setup return the view
		if (null == this.croutonView) {
			initializeCroutonView();
		}

		return croutonView;
	}

	private void measureCroutonView() {
		View view = getView();
		int widthSpec;
		if (viewGroup != null) {
			widthSpec = View.MeasureSpec.makeMeasureSpec(
					viewGroup.getMeasuredWidth(), View.MeasureSpec.AT_MOST);
		} else {
			widthSpec = View.MeasureSpec.makeMeasureSpec(activity.getWindow()
					.getDecorView().getMeasuredWidth(),
					View.MeasureSpec.AT_MOST);
		}

		view.measure(widthSpec, View.MeasureSpec.makeMeasureSpec(0,
				View.MeasureSpec.UNSPECIFIED));
	}

	private void initializeCroutonView() {
		Resources resources = this.activity.getResources();

		this.croutonView = initializeCroutonViewGroup(resources);

		// create content view
		RelativeLayout contentView = initializeContentView(resources);
		this.croutonView.addView(contentView);
	}

	private FrameLayout initializeCroutonViewGroup(Resources resources) {
		FrameLayout croutonView = new FrameLayout(this.activity);

		if (null != onClickListener) {
			croutonView.setOnClickListener(onClickListener);
		}

		final int height;
		if (this.style.heightDimensionResId > 0) {
			height = resources
					.getDimensionPixelSize(this.style.heightDimensionResId);
		} else {
			height = this.style.heightInPixels;
		}

		final int width;
		if (this.style.widthDimensionResId > 0) {
			width = resources
					.getDimensionPixelSize(this.style.widthDimensionResId);
		} else {
			width = this.style.widthInPixels;
		}

		croutonView.setLayoutParams(new FrameLayout.LayoutParams(
				width != 0 ? width : FrameLayout.LayoutParams.MATCH_PARENT,
				height));

		// set background
		if (this.style.backgroundColorValue != -1) {
			croutonView.setBackgroundColor(this.style.backgroundColorValue);
		} else {
			croutonView.setBackgroundColor(resources
					.getColor(this.style.backgroundColorResourceId));
		}

		// set the background drawable if set. This will override the background
		// color.
		if (this.style.backgroundDrawableResourceId != 0) {
			Bitmap background = BitmapFactory.decodeResource(resources,
					this.style.backgroundDrawableResourceId);
			BitmapDrawable drawable = new BitmapDrawable(resources, background);
			if (this.style.isTileEnabled) {
				drawable.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
			}
			croutonView.setBackgroundDrawable(drawable);
		}
		return croutonView;
	}

	private RelativeLayout initializeContentView(final Resources resources) {
		RelativeLayout contentView = new RelativeLayout(this.activity);
		contentView.setLayoutParams(new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT));

		// set padding
		int padding = this.style.paddingInPixels;

		// if a padding dimension has been set, this will overwrite any padding
		// in pixels
		if (this.style.paddingDimensionResId > 0) {
			padding = resources
					.getDimensionPixelSize(this.style.paddingDimensionResId);
		}
		contentView.setPadding(padding, padding, padding, padding);

		// only setup image if one is requested
		ImageView image = null;
		if ((null != this.style.imageDrawable) || (0 != this.style.imageResId)) {
			image = initializeImageView();
			contentView.addView(image, image.getLayoutParams());
		}

		TextView text = initializeTextView(resources);

		RelativeLayout.LayoutParams textParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		if (null != image) {
			textParams.addRule(RelativeLayout.RIGHT_OF, image.getId());
		}
		contentView.addView(text, textParams);
		return contentView;
	}

	private TextView initializeTextView(final Resources resources) {
		TextView text = new TextView(this.activity);
		text.setId(TEXT_ID);
		text.setText(this.text);
		text.setTypeface(Typeface.DEFAULT_BOLD);
		text.setGravity(this.style.gravity);

		// set the text color if set
		if (this.style.textColorResourceId != 0) {
			text.setTextColor(resources
					.getColor(this.style.textColorResourceId));
		}

		// Set the text size. If the user has set a text size and text
		// appearance, the text size in the text appearance
		// will override this.
		if (this.style.textSize != 0) {
			text.setTextSize(TypedValue.COMPLEX_UNIT_SP, this.style.textSize);
		}

		// Setup the shadow if requested
		if (this.style.textShadowColorResId != 0) {
			initializeTextViewShadow(resources, text);
		}

		// Set the text appearance
		if (this.style.textAppearanceResId != 0) {
			text.setTextAppearance(this.activity,
					this.style.textAppearanceResId);
		}
		return text;
	}

	private void initializeTextViewShadow(final Resources resources,
			final TextView text) {
		int textShadowColor = resources
				.getColor(this.style.textShadowColorResId);
		float textShadowRadius = this.style.textShadowRadius;
		float textShadowDx = this.style.textShadowDx;
		float textShadowDy = this.style.textShadowDy;
		text.setShadowLayer(textShadowRadius, textShadowDx, textShadowDy,
				textShadowColor);
	}

	private ImageView initializeImageView() {
		ImageView image;
		image = new ImageView(this.activity);
		image.setId(IMAGE_ID);
		image.setAdjustViewBounds(true);
		image.setScaleType(this.style.imageScaleType);

		// set the image drawable if not null
		if (null != this.style.imageDrawable) {
			image.setImageDrawable(this.style.imageDrawable);
		}

		// set the image resource if not 0. This will overwrite the drawable
		// if both are set
		if (this.style.imageResId != 0) {
			image.setImageResource(this.style.imageResId);
		}

		RelativeLayout.LayoutParams imageParams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		imageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
				RelativeLayout.TRUE);
		imageParams
				.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

		image.setLayoutParams(imageParams);

		return image;
	}
}
