package it.swim.traffic.tensor;

import recon.Form;
import recon.Value;

public abstract class TensorForm<T> extends Form<T> {
  public abstract T fromTensor(TensorDims dims, float[] tensor, int offset);

  public abstract T fromTensor(TensorDims dims, double[] tensor, int offset);

  public abstract void toTensor(T object, TensorDims dims, float[] tensor, int offset);

  public abstract void toTensor(T object, TensorDims dims, double[] tensor, int offset);

  public Value moldTensor(TensorDims dims, float[] tensor, int offset) {
    return mold(fromTensor(dims, tensor, offset));
  }

  public Value moldTensor(TensorDims dims, double[] tensor, int offset) {
    return mold(fromTensor(dims, tensor, offset));
  }

  public void castTensor(Value value, TensorDims dims, float[] tensor, int offset) {
    toTensor(cast(value), dims, tensor, offset);
  }

  public void castTensor(Value value, TensorDims dims, double[] tensor, int offset) {
    toTensor(cast(value), dims, tensor, offset);
  }
}
