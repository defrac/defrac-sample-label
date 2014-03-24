package com.defrac.sample.label;

import defrac.app.Bootstrap;
import defrac.app.GenericApp;
import defrac.display.Label;
import defrac.display.Quad;
import defrac.display.TextureData;
import defrac.event.EnterFrameEvent;
import defrac.event.Events;
import defrac.event.PointerEvent;
import defrac.event.ResourceGroupEvent;
import defrac.geom.Point;
import defrac.geom.Rectangle;
import defrac.lang.Procedure;
import defrac.resource.ResourceGroup;
import defrac.resource.StringResource;
import defrac.resource.TextureDataResource;
import defrac.text.BitmapFont;
import defrac.text.BitmapFontRenderMode;

class LabelSample extends GenericApp {
  public static void main(final String[] args) {
    Bootstrap.run(new LabelSample());
  }

  @Override
  protected void onCreate() {
    ResourceGroup<?> resources =
      ResourceGroup.of(
          StringResource.from("alpha.fnt"),
          StringResource.from("copy.fnt"),
          StringResource.from("sdf.fnt"),
          TextureDataResource.from("alpha.png"),
          TextureDataResource.from("copy.png"),
          TextureDataResource.from("sdf.png")
      );

    resources.onComplete.attach(new Procedure<ResourceGroupEvent.Complete<?>>() {
      @Override
      public void apply(ResourceGroupEvent.Complete<?> resourceGroup) {
        BitmapFont alphaFont =
          BitmapFont.fromFnt(
              (String)resourceGroup.contents.get(0),
              new TextureData[] { (TextureData)resourceGroup.contents.get(3) });

        BitmapFont copyPixelFont =
            BitmapFont.fromFnt(
                (String)resourceGroup.contents.get(1),
                new TextureData[] { (TextureData)resourceGroup.contents.get(4) });

        BitmapFont signedDistanceFieldFont =
            BitmapFont.fromFnt(
                (String)resourceGroup.contents.get(2),
                new TextureData[] { (TextureData)resourceGroup.contents.get(5) });

        onResourcesComplete(alphaFont, copyPixelFont, signedDistanceFieldFont);
      }
    });

    resources.load();
  }

  private void onResourcesComplete(BitmapFont alphaFont, BitmapFont copyPixelFont, BitmapFont sdfFont) {
    final Quad quad = addChild(new Quad(256.0f, 256.0f, 0xff602020));
    final Label alphaFontLabel = addChild(new Label());
    final Label copyPixelLabel = addChild(new Label());
    final Label sdfLabel = addChild(new Label());
    final Label dynamicLabel = addChild(new Label());
    final Rectangle rect = new Rectangle();
    final Point point = new Point();
    float padding = 8.0f;
    final String message =
        "This is some text to demonstrate word wrap. Move your mouse or drag your " +
        "finger across the screen (only if its a touch screen ;)) to " +
        "resize the text box. Press or touch to switch between different " +
        "alignments. Current alignment: ";

    alphaFontLabel.
        font(alphaFont).
        color(0xffe0e0e0).
        text("This text is rendered using an alpha mask.");

    copyPixelFont.renderMode = BitmapFontRenderMode.COPY;
    copyPixelLabel.
        font(copyPixelFont).
        color(0xffffffff).
        text("This text is rendered by copying pixels.");

    sdfFont.renderMode = BitmapFontRenderMode.SIGNED_DISTANCE_FIELD;
    sdfFont.sdfSpread = 6.0f;
    sdfLabel.
        font(sdfFont).
        color(0xffe0e0e0).
        text("This text is rendered using a signed distance field.");

    dynamicLabel.
        font(sdfFont).
        color(0xffe0e0e0).
        text(message+"LEFT");

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

    Events.onEnterFrame.attach(new Procedure<EnterFrameEvent>() {
      @Override
      public void apply(EnterFrameEvent event) {
        float sin = (float) Math.sin(event.frame * 0.01f);
        sdfLabel.scaleTo(1.0f + 0.25f * sin, 1.0f + 0.25f * sin);

        stage().eventManager().pointerPos(point, 0);

        point.x -= rect.x;
        point.y -= rect.y;

        if(point.x > 0.0f && point.y > 0.0f) {
          dynamicLabel.
              size(point.x, point.y).
              aabb(stage(), rect);

          quad.
              size(rect.width, rect.height).
              moveTo(rect.x, rect.y);
        }
      }
    });

    Events.onPointerDown.attach(new Procedure<PointerEvent>() {
      @Override
      public void apply(PointerEvent pointerEvent) {
        Label.AlignHorizontal align;

        switch(dynamicLabel.alignHorizontal()) {
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
