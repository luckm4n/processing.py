package jycessing.build;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class PolymorphicMethod {
	private final String name;
	private final int arity;
	private final List<Signature> signatures = new ArrayList<Signature>();

	public PolymorphicMethod(final String name, final int arity) {
		this.name = name;
		this.arity = arity;
	}

	public void add(final Method method) {
		if (method.getParameterTypes().length != arity) {
			throw new IllegalArgumentException("I expect methods with " + arity
					+ " args, but got " + method);
		}
		signatures.add(new Signature(method));
	}

	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(String.format("\t\t\tcase %d: \n\t", arity));
		if (signatures.size() == 1) {
			sb.append("\t\t\t");
			append(signatures.get(0), sb);
		} else {
			for (int i = 0; i < signatures.size(); i++) {
				final Signature sig = signatures.get(i);
				if (i > 0) {
					sb.append(" else ");
				} else {
					sb.append("\t\t\t");
				}
				sb.append("if (");
				for (int j = 0; j < arity; j++) {
					if (j > 0) {
						sb.append(" && ");
					}
					final String typeExpr = String.format("args[%d].getType()", j);
					sb.append(sig.getTypecheckExpression(j, typeExpr));
				}
				sb.append(") {\n\t\t\t\t\t");
				append(sig, sb);
				sb.append("\n\t\t\t\t}");
			}
			sb
					.append(" else { throw new IllegalArgumentException(\"Couldn't figure out which \\\"");
			sb.append(name);
			sb.append("\\\" to call.\"); }\n");
		}
		return sb.toString();
	}

	private void append(final Signature signature, final StringBuilder sb) {
		if (!signature.isVoid()) {
			sb.append("return ").append(TypeUtil.pyConversionPrefix(signature.getReturnType()));
		}
		sb.append(name).append('(');
		for (int i = 0; i < arity; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(TypeUtil.asJavaExpression("args[" + i + "]", signature.getArgTypes().get(
					i)));
		}
		sb.append(')');
		if (signature.isVoid()) {
			sb.append(";\n");
			sb.append("\t\t\t\treturn Py.None;\n");
		} else {
			sb.append(");");
		}
	}

}