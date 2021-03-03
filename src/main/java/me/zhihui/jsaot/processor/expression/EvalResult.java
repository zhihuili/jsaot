package me.zhihui.jsaot.processor.expression;

public class EvalResult implements Cloneable {

	public enum EvalResultType {
		LONG, FLOAT, STRING, BOOL, ARRAY, AST
	}

	private EvalResultType type;
	private Object value;

	public EvalResult() {
	}

	public EvalResult(EvalResultType type, Object value) {
		this.type = type;
		this.value = value;
	}

	public EvalResultType getType() {
		return type;
	}

	public void setType(EvalResultType type) {
		this.type = type;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public boolean isNumber() {
		if (type == EvalResultType.LONG || type == EvalResultType.FLOAT) {
			return true;
		}
		return false;
	}

	public void increase() {
		if (type == EvalResultType.LONG)
			value = Long.valueOf(value.toString()) + 1;
	}

	public void decrease() {
		if (type == EvalResultType.LONG)
			value = Long.valueOf(value.toString()) - 1;
	}

	@Override
	public String toString() {
		return type + ":" + value;
	}

	@Override
	public EvalResult clone() throws CloneNotSupportedException {
		return (EvalResult) super.clone();
	}

	public EvalResult value() {
		try {
			if (type == EvalResultType.STRING || type == EvalResultType.LONG
					|| type == EvalResultType.FLOAT)
				return clone();
			else
				return this;
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}

}
