package sh.ball.audio;

import sh.ball.audio.effect.Effect;
import xt.audio.*;
import xt.audio.Enums.XtSample;
import xt.audio.Enums.XtSetup;
import xt.audio.Enums.XtSystem;
import xt.audio.Structs.XtBuffer;
import xt.audio.Structs.XtBufferSize;
import xt.audio.Structs.XtChannels;
import xt.audio.Structs.XtDeviceStreamParams;
import xt.audio.Structs.XtFormat;
import xt.audio.Structs.XtMix;
import xt.audio.Structs.XtStreamParams;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import sh.ball.shapes.Shape;
import sh.ball.shapes.Vector2;

import java.util.List;

public class AudioPlayer implements Renderer<List<Shape>> {

  private static final int BUFFER_SIZE = 20;

  private final XtFormat format;
  private final BlockingQueue<List<Shape>> frameQueue = new ArrayBlockingQueue<>(BUFFER_SIZE);
  private final Map<Object, Effect> effects = new HashMap<>();

  private List<Shape> frame;
  private int currentShape = 0;
  private int audioFramesDrawn = 0;

  private double weight = Shape.DEFAULT_WEIGHT;

  private volatile boolean stopped;

  public AudioPlayer(int sampleRate) {
    XtMix mix = new XtMix(sampleRate, XtSample.FLOAT32);
    XtChannels channels = new XtChannels(0, 0, 2, 0);
    this.format = new XtFormat(mix, channels);
  }

  private int render(XtStream stream, XtBuffer buffer, Object user) throws InterruptedException {
    XtSafeBuffer safe = XtSafeBuffer.get(stream);
    safe.lock(buffer);
    float[] output = (float[]) safe.getOutput();

    for (int f = 0; f < buffer.frames; f++) {
      Shape shape = getCurrentShape();

      shape = shape.setWeight(weight);

      double totalAudioFrames = shape.getWeight() * shape.getLength();
      double drawingProgress = totalAudioFrames == 0 ? 1 : audioFramesDrawn / totalAudioFrames;
      Vector2 nextVector = applyEffects(f, shape.nextVector(drawingProgress));

      output[f * format.channels.outputs] = (float) nextVector.getX();
      output[f * format.channels.outputs + 1] = (float) nextVector.getY();

      audioFramesDrawn++;

      if (audioFramesDrawn > totalAudioFrames) {
        audioFramesDrawn = 0;
        currentShape++;
      }

      if (currentShape >= frame.size()) {
        currentShape = 0;
        frame = frameQueue.take();
      }
    }
    safe.unlock(buffer);
    return 0;
  }

  private Vector2 applyEffects(int frame, Vector2 vector) {
    for (Effect effect : effects.values()) {
      vector = effect.apply(frame, vector);
    }
    return vector;
  }

  @Override
  public void setQuality(double quality) {
    this.weight = quality;
  }

  private Shape getCurrentShape() {
    if (frame.size() == 0) {
      return new Vector2();
    }

    return frame.get(currentShape);
  }

  @Override
  public void run() {
    try {
      frame = frameQueue.take();
    } catch (InterruptedException e) {
      throw new RuntimeException("Initial frame not found. Cannot continue.");
    }

    try (XtPlatform platform = XtAudio.init(null, null)) {
      XtSystem system = platform.setupToSystem(XtSetup.CONSUMER_AUDIO);
      XtService service = platform.getService(system);
      if (service == null) return;

      String defaultOutput = service.getDefaultDeviceId(true);
      if (defaultOutput == null) return;

      try (XtDevice device = service.openDevice(defaultOutput)) {
        if (device.supportsFormat(format)) {

          XtBufferSize size = device.getBufferSize(format);
          XtStreamParams streamParams = new XtStreamParams(true, this::render, null, null);
          XtDeviceStreamParams deviceParams = new XtDeviceStreamParams(streamParams, format, size.current);
          try (XtStream stream = device.openStream(deviceParams, null);
               XtSafeBuffer safe = XtSafeBuffer.register(stream, true)) {
            stream.start();
            while (!stopped) {
              Thread.onSpinWait();
            }
            stream.stop();
          }
        }
      }
    }
  }

  @Override
  public void stop() {
    stopped = true;
  }

  @Override
  public void addFrame(List<Shape> frame) {
    try {
      frameQueue.put(frame);
    } catch (InterruptedException e) {
      e.printStackTrace();
      System.err.println("Frame missed.");
    }
  }

  @Override
  public void flushFrames() {
    frameQueue.clear();
  }

  @Override
  public void addEffect(Object identifier, Effect effect) {
    effects.put(identifier, effect);
  }

  @Override
  public void removeEffect(Object identifier) {
    effects.remove(identifier);
  }

}
