package me.zhihui.jsaot.processor.expression;

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
}
