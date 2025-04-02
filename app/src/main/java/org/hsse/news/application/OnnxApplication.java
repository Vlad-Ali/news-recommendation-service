package org.hsse.news.application;

import ai.onnxruntime.OrtException;
import org.hsse.news.model.OnnxModelRunner;
import org.hsse.news.util.ResourceUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class OnnxApplication {
  public static final String MODEL_PATH = ResourceUtil.getResource("/onnx_model/trfs-model.onnx");
  public static final String TOKENIZER_PATH = ResourceUtil.getResource("/onnx_model/tokenizer/tokenizer.json");
  private final OnnxModelRunner modelRunner;


  public OnnxApplication() throws IOException, OrtException {
    this.modelRunner = new OnnxModelRunner(MODEL_PATH, TOKENIZER_PATH);
  }

  private Map<String, Float> getResult(final String text, final List<String> labels) throws OrtException {
    return modelRunner.runModel(text, labels);
  }

  public Map<String, Float> predict(final String text, final List<String> labels) throws OrtException {
    final String query = encodeString(text);
    return getResult(query, labels);
  }

  private String encodeString(final String input) {
    final byte[] bytes = input.getBytes();
    return new String(bytes, StandardCharsets.UTF_8);
  }
}
