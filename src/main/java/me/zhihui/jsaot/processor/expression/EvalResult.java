package me.zhihui.jsaot.processor.expression;

public class EvalResult {

	public enum EvalResultType {
		LONG, FLOAT, STRING, BOOL
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

	@Override
	public String toString() {
		return type + ":" + value;
	}

}
