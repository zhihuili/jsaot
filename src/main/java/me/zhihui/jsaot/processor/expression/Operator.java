package me.zhihui.jsaot.processor.expression;

import me.zhihui.jsaot.processor.expression.EvalResult.EvalResultType;

public class Operator {
	public static EvalResult add(EvalResult a, EvalResult b) {
		EvalResult result = new EvalResult();
		if (a.getType() == EvalResult.EvalResultType.STRING
				|| b.getType() == EvalResult.EvalResultType.STRING) {
			result.setType(EvalResult.EvalResultType.STRING);
			result.setValue(a.getValue().toString() + b.getValue().toString());

		} else if (a.getType() == EvalResult.EvalResultType.FLOAT
				|| b.getType() == EvalResult.EvalResultType.FLOAT) {
			result.setType(EvalResult.EvalResultType.FLOAT);
			result.setValue(Float.valueOf(a.getValue().toString())
					+ Float.valueOf(b.getValue().toString()));

		} else {
			result.setType(EvalResult.EvalResultType.LONG);
			result.setValue(Long.valueOf(a.getValue().toString())
					+ Long.valueOf(b.getValue().toString()));
		}
		return result;
	}

	public static EvalResult minus(EvalResult a, EvalResult b) {
		EvalResult result = new EvalResult();
		if (a.getType() == EvalResult.EvalResultType.FLOAT
				|| b.getType() == EvalResult.EvalResultType.FLOAT) {
			result.setType(EvalResult.EvalResultType.FLOAT);
			result.setValue(Float.valueOf(a.getValue().toString())
					- Float.valueOf(b.getValue().toString()));

		} else {
			result.setType(EvalResult.EvalResultType.LONG);
			result.setValue(Long.valueOf(a.getValue().toString())
					- Long.valueOf(b.getValue().toString()));
		}
		return result;
	}

	public static EvalResult multi(EvalResult a, EvalResult b) {
		EvalResult result = new EvalResult();
		if (a.getType() == EvalResult.EvalResultType.FLOAT
				|| b.getType() == EvalResult.EvalResultType.FLOAT) {
			result.setType(EvalResult.EvalResultType.FLOAT);
			result.setValue(Float.valueOf(a.getValue().toString())
					* Float.valueOf(b.getValue().toString()));

		} else {
			result.setType(EvalResult.EvalResultType.LONG);
			result.setValue(Long.valueOf(a.getValue().toString())
					* Long.valueOf(b.getValue().toString()));
		}
		return result;
	}

	public static EvalResult divide(EvalResult a, EvalResult b) {
		EvalResult result = new EvalResult();

		result.setType(EvalResult.EvalResultType.FLOAT);
		result.setValue(Float.valueOf(a.getValue().toString())
				/ Float.valueOf(b.getValue().toString()));

		return result;
	}

	public static EvalResult mod(EvalResult a, EvalResult b) {
		EvalResult result = new EvalResult();

		result.setType(EvalResult.EvalResultType.LONG);
		result.setValue(Long.valueOf(a.getValue().toString())
				% Long.valueOf(b.getValue().toString()));

		return result;
	}

	public static EvalResult relational(String op, EvalResult a, EvalResult b) {
		EvalResult r = new EvalResult();
		r.setType(EvalResultType.BOOL);
		if (a.isNumber() && b.isNumber()) {
			switch (op) {
			case "<":
				if (Float.valueOf(a.getValue().toString()) < Float.valueOf(b
						.getValue().toString())) {
					r.setValue(true);
				} else {
					r.setValue(false);
				}
				break;
			case "<=":
				if (Float.valueOf(a.getValue().toString()) <= Float.valueOf(b
						.getValue().toString())) {
					r.setValue(true);
				} else {
					r.setValue(false);
				}
				break;
			case ">":
				if (Float.valueOf(a.getValue().toString()) > Float.valueOf(b
						.getValue().toString())) {
					r.setValue(true);
				} else {
					r.setValue(false);
				}
				break;
			}

		}
		return r;
	}
}
