package it.swim.traffic.model;

import it.swim.traffic.tensor.TensorDims;
import it.swim.traffic.tensor.TensorForm;
import recon.Record;
import recon.Value;
import swim.util.Murmur3;

public final class VehicleDetectorTensor {
  public double occupancy;
  public int count;

  public VehicleDetectorTensor(double occupancy, int count) {
    this.occupancy = occupancy;
    this.count = count;
  }

  public VehicleDetectorTensor() {
    this(0.0, 0);
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  @Override
  public VehicleDetectorTensor clone() {
    return new VehicleDetectorTensor(occupancy, count);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof VehicleDetectorTensor) {
      final VehicleDetectorTensor that = (VehicleDetectorTensor)other;
      return occupancy == that.occupancy && count == that.count;
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return Murmur3.mash(Murmur3.mix(Murmur3.mix(0x84B2D172,
        Murmur3.hash(occupancy)), Murmur3.hash(count)));
  }

  @Override
  public String toString() {
    return "new" + ' ' + "VehicleDetectorTensor" + '(' + occupancy + ", " + count + ')';
  }

  public static final TensorForm<VehicleDetectorTensor> FORM = new VehicleDetectorTensorForm();

  public static final int TENSOR_SIZE = 1;
}

final class VehicleDetectorTensorForm extends TensorForm<VehicleDetectorTensor> {
  @Override
  public Class<?> getType() {
    return VehicleDetectorTensor.class;
  }

  @Override
  public VehicleDetectorTensor getDefault() {
    return new VehicleDetectorTensor();
  }

  @Override
  public Value mold(VehicleDetectorTensor tensor) {
    return Record.empty(2)
        .slot("occupancy", tensor.occupancy)
        .slot("count", tensor.count);
  }

  @Override
  public VehicleDetectorTensor cast(Value value) {
    final double occupancy = value.get("occupancy").doubleValue(Double.NaN);
    final int count = value.get("count").intValue(-1);
    if (!Double.isNaN(occupancy) && count >= 0) {
      return new VehicleDetectorTensor(occupancy, count);
    } else {
      return null;
    }
  }

  @Override
  public void toTensor(VehicleDetectorTensor object, TensorDims dims, float[] tensor, int offset) {
    tensor[offset] = (float)object.occupancy;
  }

  @Override
  public void toTensor(VehicleDetectorTensor object, TensorDims dims, double[] tensor, int offset) {
    tensor[offset] = object.occupancy;
  }

  @Override
  public VehicleDetectorTensor fromTensor(TensorDims dims, float[] tensor, int offset) {
    final double occupancy = tensor[offset];
    return new VehicleDetectorTensor(occupancy, 0);
  }

  @Override
  public VehicleDetectorTensor fromTensor(TensorDims dims, double[] tensor, int offset) {
    final double occupancy = tensor[offset];
    return new VehicleDetectorTensor(occupancy, 0);
  }
}
