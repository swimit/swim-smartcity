package it.swim.traffic.model;

import it.swim.traffic.tensor.TensorDims;
import it.swim.traffic.tensor.TensorForm;
import recon.Record;
import recon.Value;
import swim.util.Murmur3;

public final class SignalPhaseTensor {
  public double red;
  public double yellow;
  public double green;

  public SignalPhaseTensor(double red, double yellow, double green) {
    this.red = red;
    this.yellow = yellow;
    this.green = green;
  }

  public SignalPhaseTensor() {
    this(0.0, 0.0, 0.0);
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  @Override
  public SignalPhaseTensor clone() {
    return new SignalPhaseTensor(red, yellow, green);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof SignalPhaseTensor) {
      final SignalPhaseTensor that = (SignalPhaseTensor)other;
      return red == that.red && yellow == that.yellow && green == that.green;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Murmur3.mash(Murmur3.mix(Murmur3.mix(Murmur3.mix(0x7A3554D3,
        Murmur3.hash(red)), Murmur3.hash(yellow)), Murmur3.hash(green)));
  }

  @Override
  public String toString() {
    return "new" + ' ' + "SignalPhaseTensor" + '('
        + red + ", " + yellow + ", " + green + ')';
  }

  public static final TensorForm<SignalPhaseTensor> FORM = new SignalPhaseTensorForm();

  public static final int TENSOR_SIZE = 3;
}

final class SignalPhaseTensorForm extends TensorForm<SignalPhaseTensor> {
  @Override
  public Class<?> getType() {
    return SignalPhaseTensor.class;
  }

  @Override
  public SignalPhaseTensor getDefault() {
    return new SignalPhaseTensor();
  }

  @Override
  public Value mold(SignalPhaseTensor tensor) {
    return Record.empty(3)
        .slot("red", tensor.red)
        .slot("yellow", tensor.yellow)
        .slot("green", tensor.green);
  }

  @Override
  public SignalPhaseTensor cast(Value value) {
    final double red = value.get("red").doubleValue(Double.NaN);
    final double yellow = value.get("yellow").doubleValue(Double.NaN);
    final double green = value.get("green").doubleValue(Double.NaN);
    if (!Double.isNaN(red) && !Double.isNaN(yellow) && !Double.isNaN(green)) {
      return new SignalPhaseTensor(red, yellow, green);
    } else {
      return null;
    }
  }

  @Override
  public void toTensor(SignalPhaseTensor object, TensorDims dims, float[] tensor, int offset) {
    tensor[offset] = (float)object.red;
    offset += dims.stride();
    tensor[offset] = (float)object.yellow;
    offset += dims.stride();
    tensor[offset] = (float)object.green;
  }

  @Override
  public void toTensor(SignalPhaseTensor object, TensorDims dims, double[] tensor, int offset) {
    tensor[offset] = object.red;
    offset += dims.stride();
    tensor[offset] = object.yellow;
    offset += dims.stride();
    tensor[offset] = object.green;
  }

  @Override
  public SignalPhaseTensor fromTensor(TensorDims dims, float[] tensor, int offset) {
    final double red = tensor[offset];
    offset += dims.stride();
    final double yellow = tensor[offset];
    offset += dims.stride();
    final double green = tensor[offset];
    return new SignalPhaseTensor(red, yellow, green);
  }

  @Override
  public SignalPhaseTensor fromTensor(TensorDims dims, double[] tensor, int offset) {
    final double red = tensor[offset];
    offset += dims.stride();
    final double yellow = tensor[offset];
    offset += dims.stride();
    final double green = tensor[offset];
    return new SignalPhaseTensor(red, yellow, green);
  }
}
