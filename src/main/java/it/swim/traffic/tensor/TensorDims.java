package it.swim.traffic.tensor;;

import recon.Form;
import recon.Item;
import recon.Record;
import recon.Value;
import swim.util.Murmur3;

public final class TensorDims {
  public final int size;
  public final int stride;
  final TensorDims next;

  TensorDims(int size, int stride, TensorDims next) {
    this.size = size;
    this.stride = stride;
    this.next = next;
  }

  public boolean isDefined() {
    return size != 0 || stride != 0 || next != null;
  }

  public int count() {
    int i = 0;
    TensorDims dim = this;
    do {
      i += 1;
      dim = dim.next;
    } while (dim != null);
    return i;
  }

  public int size() {
    return size;
  }

  public int stride() {
    return stride;
  }

  public TensorDims next() {
    return next;
  }

  public boolean isPacked() {
    return stride == (next != null ? next.size * next.stride : 1);
  }

  public boolean isFullyPacked() {
    TensorDims dim = this;
    do {
      if (!dim.isPacked()) {
        return false;
      }
      dim = dim.next;
    } while (dim != null);
    return true;
  }

  public TensorDims by(int size, int stride) {
    if (!isDefined()) {
      return of(size, stride);
    } else if (stride == this.size * this.stride) {
      return by(size);
    } else {
      return new TensorDims(size, stride, this);
    }
  }

  public TensorDims by(int size) {
    if (!isDefined()) {
      return of(size);
    } else if (size == 2 && this == D2) {
      return D2x2;
    } else if (size == 3 && this == D3) {
      return D3x3;
    } else if (size == 4 && this == D4) {
      return D4x4;
    } else {
      return new TensorDims(size, this.size * this.stride, this);
    }
  }

  public TensorDims flattened() {
    TensorDims dim = this;
    int size = 1;
    do {
      if (dim.stride == (dim.next != null ? dim.next.size * dim.next.stride : 1)) {
        size *= dim.size;
      } else {
        throw new DimensionException("flattened sparse dimensions");
      }
      dim = dim.next;
    } while (dim != null);
    return of(size);
  }

  public int[] toSizeArray(int[] sizes) {
    int i = 0;
    TensorDims dim = this;
    do {
      sizes[i] = dim.size;
      i += 1;
      dim = dim.next;
    } while (dim != null);
    return sizes;
  }

  public int[] toSizeArray() {
    final int[] sizes = new int[count()];
    return toSizeArray(sizes);
  }

  public int[] toStrideArray(int[] strides) {
    int i = 0;
    TensorDims dim = this;
    do {
      strides[i] = dim.stride;
      i += 1;
      dim = dim.next;
    } while (dim != null);
    return strides;
  }

  public int[] toStrideArray() {
    final int[] strides = new int[count()];
    return toStrideArray(strides);
  }

  public Value toValue() {
    return FORM.mold(this);
  }

  public boolean conforms(TensorDims that) {
    return conforms(this, that);
  }
  static boolean conforms(TensorDims lhs, TensorDims rhs) {
    do {
      if (lhs.size != rhs.size) {
        return false;
      }
      lhs = lhs.next;
      rhs = rhs.next;
    } while (lhs != null && rhs != null);
    return lhs == null && rhs == null;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    } else if (other instanceof TensorDims) {
      return equals(this, (TensorDims) other);
    } else {
      return false;
    }
  }
  static boolean equals(TensorDims lhs, TensorDims rhs) {
    do {
      if (lhs.size != rhs.size || lhs.stride != rhs.stride) {
        return false;
      }
      lhs = lhs.next;
      rhs = rhs.next;
    } while (lhs != null && rhs != null);
    return lhs == null && rhs == null;
  }

  @Override
  public int hashCode() {
    return Murmur3.mash(hash(0x6C6495FA, this));
  }
  static int hash(int code, TensorDims dim) {
    do {
      code = Murmur3.mix(Murmur3.mix(code, dim.size), dim.stride);
    } while (dim != null);
    return code;
  }

  void appendString(StringBuilder s) {
    if (next != null) {
      next.appendString(s);
      s.append('.').append("by").append('(').append(size);
      if (!isPacked()) {
        s.append(", ").append(stride);
      }
      s.append(')');
    } else {
      s.append("TensorDims").append('.').append("of").append('(').append(size);
      if (!isPacked()) {
        s.append(", ").append(stride);
      }
      s.append(')');
    }
  }

  @Override
  public String toString() {
    final StringBuilder s = new StringBuilder();
    appendString(s);
    return s.toString();
  }


  public static final Form<TensorDims> FORM = new TensorDimsForm();

  public static final TensorDims UNDEFINED = new TensorDims(0, 0, null);
  public static final TensorDims D1 = new TensorDims(1, 1, null);
  public static final TensorDims D2 = new TensorDims(2, 1, null);
  public static final TensorDims D3 = new TensorDims(3, 1, null);
  public static final TensorDims D4 = new TensorDims(4, 1, null);
  @SuppressWarnings("checkstyle:ConstantName")
  public static final TensorDims D2x2 = new TensorDims(2, 2, D2);
  @SuppressWarnings("checkstyle:ConstantName")
  public static final TensorDims D3x3 = new TensorDims(3, 3, D3);
  @SuppressWarnings("checkstyle:ConstantName")
  public static final TensorDims D4x4 = new TensorDims(4, 4, D4);

  public static TensorDims of(int size, int stride) {
    if (size == 0 && stride == 0) {
      return UNDEFINED;
    } else if (stride == 1) {
      return of(size);
    } else {
      return new TensorDims(size, stride, null);
    }
  }

  public static TensorDims of(int size) {
    if (size == 0) {
      return UNDEFINED;
    } else if (size == 1) {
      return D1;
    } else if (size == 2) {
      return D2;
    } else if (size == 3) {
      return D3;
    } else if (size == 4) {
      return D4;
    } else {
      return new TensorDims(size, 1, null);
    }
  }
}

final class TensorDimsForm extends Form<TensorDims> {
  @Override
  public String getTag() {
    return "dim";
  }

  @Override
  public Class<?> getType() {
    return TensorDims.class;
  }

  @Override
  public TensorDims getDefault() {
    return TensorDims.UNDEFINED;
  }

  @Override
  public Value mold(TensorDims dim) {
    final Record record = Record.empty(dim.count());
    do {
      final Record header = Record.empty(2);
      header.slot("size", dim.size);
      if (!dim.isPacked()) {
        header.slot("stride", dim.stride);
      }
      record.attr(getTag(), header);
      dim = dim.next;
    } while (dim != null);
    return record;
  }

  @Override
  public TensorDims cast(Value value) {
    TensorDims next = null;
    for (int i = value.length() - 1; i >= 0; i -= 1) {
      final Item item = value.getItem(i);
      if (item.isAttr() && item.getName().equals(getTag())) {
        final Value header = item.getValue();
        final Value size = header.get("size");
        if (size.isNumber()) {
          final Value stride = header.get("stride");
          if (stride.isNumber()) {
            if (next != null) {
              next = next.by(size.intValue(), stride.intValue());
            } else {
              next = TensorDims.of(size.intValue(), stride.intValue());
            }
          } else if (next != null) {
            next = next.by(size.intValue());
          } else {
            next = TensorDims.of(size.intValue());
          }
        }
      }
    }
    return next;
  }
}
