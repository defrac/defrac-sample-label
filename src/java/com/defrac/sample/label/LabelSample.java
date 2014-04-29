package com.defrac.sample.label;

import defrac.app.Bootstrap;
import defrac.app.GenericApp;
import defrac.display.Label;
import defrac.display.Quad;
import defrac.display.TextureData;
import defrac.event.EnterFrameEvent;
import defrac.event.EventListener;
import defrac.event.Events;
import defrac.event.PointerEvent;
import defrac.geom.Point;
import defrac.geom.Rectangle;
import defrac.resource.ResourceGroup;
import defrac.resource.StringResource;
import defrac.resource.TextureDataResource;
import defrac.text.BitmapFont;
import defrac.text.BitmapFontRenderMode;

import java.util.List;

class LabelSample extends GenericApp {
  public static void main(final String[] args) {
    Bootstrap.run(new LabelSample());
  }

  @Override
  protected void onCreate() {
    // First of all we load all resources necessary for this sample
    // If you just use the alpha mask feature a neat shortcut to
    // set a font is also label.font(BitmapFont.fromFnt("alpha.fnt", "alpha.png"))
    //
    // Usually you want to load a font upfront so you have it immediately available
    // and the bounds of your label may be measured.
    ResourceGroup<Object> resources =
      ResourceGroup.of(
          StringResource.from("alpha.fnt"),
          StringResource.from("copy.fnt"),
          StringResource.from("sdf.fnt"),
          TextureDataResource.from("alpha.png"),
          TextureDataResource.from("copy.png"),
          TextureDataResource.from("sdf.png")
      );

    resources.listener(new ResourceGroup.SimpleListener<Object>() {
      @Override
      public void onResourceGroupComplete(ResourceGroup<Object> group, List<Object> content) {
        // When all resources are loaded we create the BitmapFont
        // objects for them.
        //
        // Nothing special here so far.
        BitmapFont alphaFont =
          BitmapFont.fromFnt(
              (String)content.get(0),
              new TextureData[] { (TextureData)content.get(3) });

        BitmapFont copyPixelFont =
            BitmapFont.fromFnt(
                (String)content.get(1),
                new TextureData[] { (TextureData)content.get(4) });

        BitmapFont signedDistanceFieldFont =
            BitmapFont.fromFnt(
                (String)content.get(2),
                new TextureData[] { (TextureData)content.get(5) });

        onResourcesComplete(alphaFont, copyPixelFont, signedDistanceFieldFont);
      }
    });

    resources.load();
  }

  void onResourcesComplete(BitmapFont alphaFont,
                           BitmapFont copyPixelFont,
                           BitmapFont sdfFont) {
    float padding = 8.0f;

    // We will need a rectangle to store the bounds of our labels
    // and a point which we use to obtain pointer coordinates
    final Rectangle rect = new Rectangle();
    final Point point = new Point();

    // The quad will be used to show the bounds of the label that is
    // dynamically sized
    final Quad quad = addChild(new Quad(256.0f, 256.0f, 0xff602020));

    // Finally create our labels
    final Label alphaFontLabel = addChild(new Label());
    final Label copyPixelLabel = addChild(new Label());
    final Label sdfLabel = addChild(new Label());
    final Label dynamicLabel = addChild(new Label());

    final String message =
        "This is some text to demonstrate word wrap. Move your mouse or drag your " +
        "finger across the screen (only if its a touch screen ;)) to " +
        "resize the text box. Press or touch to switch between different " +
        "alignments. Current alignment: ";

    // Rendering via an alpha-mask is the default mode and no additional
    // setup is necessary
    alphaFontLabel.
        font(alphaFont).
        color(0xffe0e0e0).
        text("This text is rendered using an alpha mask.");

    // For the copy-pixel mode, we need to tell that the font should be
    // rendered using the BitmapFontRenderMode.COPY mode
    // You might also want to change the color to 0xffffffff since it is
    // used to multiply the final color and can be used to change the style
    // of your font.
    copyPixelFont.renderMode = BitmapFontRenderMode.COPY;
    copyPixelLabel.
        font(copyPixelFont).
        color(0xffffffff).
        text("This text is rendered by copying pixels.");

    // For signed distance fields we also have to choose the correct render
    // mode which is BitmapFontRenderMode.SIGNED_DISTANCE_FIELD. A very important
    // aspect is the sdfSpread parameter. You have to fill in the value you used
    // when generating your signed distance field.
    sdfFont.renderMode = BitmapFontRenderMode.SIGNED_DISTANCE_FIELD;
    sdfFont.sdfSpread = 6.0f;
    sdfLabel.
        font(sdfFont).
        color(0xffe0e0e0).
        text("This text is rendered using a signed distance field.");

    // The dynamic label uses the sdfFont -- the render mode of this font
    // has already been set so there is nothing special to do
    dynamicLabel.
        font(sdfFont).
        color(0xffe0e0e0).
        text(message+"LEFT");

    // At this point we do just some layout of the labels
    // placing them below each other
    alphaFontLabel.
        moveTo(padding, padding).
        aabb(stage(), rect);

    copyPixelLabel.
        moveTo(padding, rect.bottom() + padding).
        aabb(stage(), rect);

    sdfLabel.
        moveTo(padding, rect.bottom() + padding).
        aabb(stage(), rect);

    dynamicLabel.
        moveTo(padding, rect.y + rect.height * 1.25f + padding).aabb(stage(), rect);

    quad.size(rect.width, rect.height).moveTo(rect.x, rect.y);

    Events.onEnterFrame.add(new EventListener<EnterFrameEvent>() {
      @Override
      public void onEvent(EnterFrameEvent event) {
        // Each frame we want to animate the sdfLabel and scale it to
        // a different size.
        float sin = (float) Math.sin(event.frame * 0.01f);
        sdfLabel.scaleTo(1.0f + 0.25f * sin, 1.0f + 0.25f * sin);

        // The dynamic label shall be sized by the pointer coordinates.
        // Therefore we ask the event manager for the coordinates of
        // pointer 0.
        stage().eventManager().pointerPos(point, 0);

        // .. offset those coordinates by the bounds of the label
        point.x -= rect.x;
        point.y -= rect.y;

        if (point.x > 0.0f && point.y > 0.0f) {
          // ... and if they are still valid we update the size of the
          //     label. It would be perfectly valid to change its
          //     width only. The dynamicLabel.autoSize() maybe be changed
          //     if you would want to switch back to a different mode.
          dynamicLabel.
              size(point.x, point.y).
              aabb(stage(), rect);

          // Do not forget to show the bounds
          quad.
              size(rect.width, rect.height).
              moveTo(rect.x, rect.y);
        }
      }
    });

    Events.onPointerDown.add(new EventListener<PointerEvent>() {
      @Override
      public void onEvent(PointerEvent pointerEvent) {
        // Each time the user touches the screen (or clicks the mouse button)
        // we want to switch the alignment of the label element.
        Label.AlignHorizontal align;

        switch (dynamicLabel.alignHorizontal()) {
          case LEFT:
            align = Label.AlignHorizontal.CENTER;
            break;
          case CENTER:
            align = Label.AlignHorizontal.RIGHT;
            break;
          case RIGHT:
            align = Label.AlignHorizontal.LEFT;
            break;
          default:
            throw new IllegalStateException();
        }

        dynamicLabel.
            alignHorizontal(align).
            text(message + align);
      }
    });
  }
}
