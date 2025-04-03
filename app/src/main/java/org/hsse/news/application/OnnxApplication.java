package org.hsse.news.application;

import ai.onnxruntime.OrtException;
import org.hsse.news.model.OnnxModelRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class OnnxApplication {
  private final OnnxModelRunner modelRunner;


  public OnnxApplication(@Value("classpath:onnx_model/trfs-model.onnx") final Resource modelPath, @Value("classpath:onnx_model/tokenizer/tokenizer.json") final Resource tokenizerPath) throws IOException, OrtException {
    this.modelRunner = new OnnxModelRunner(modelPath.getURI().getPath().substring(1), tokenizerPath.getURI().getPath().substring(1));
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
