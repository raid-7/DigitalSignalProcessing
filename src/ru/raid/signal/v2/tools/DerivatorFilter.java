package ru.raid.signal.v2.tools;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularMatrixException;

import ru.raid.signal.v2.Filter;
import ru.raid.signal.v2.Signal;

public class DerivatorFilter implements Filter {
	private int left, right;
	private int power;
	
	public DerivatorFilter(int l, int r, int pow) {
		left = l;
		right = r;
		power = pow;
	}

	@Override
	public int getBeforeOffset() {
		return left;
	}

	@Override
	public int getAfterOffset() {
		return right;
	}

	@Override
	public double filter(Signal signal, double[] data, int pos) {
		double[][] sys = new double[power+1][power+1];
		double[] eqs = new double[power+1];
		double[] timeSums = new double[2*power+1];
		for (int i=0; i<=2*power; i++) {
			timeSums[i] = computeTimePower(pos, i, signal.getSamplePeriod());
		}
		for (int i=0; i<=power; i++) {
			for (int j=0; j<=power; j++) {
				sys[i][j] = timeSums[i+j];
			}
			eqs[i] = computeValueTimePowerSum(data, pos, i, signal.getSamplePeriod());
		}
		
		double[] solution = solveSystem(sys, eqs);
		if (solution == null) {
			return Double.NaN;
		}
		PolynomialFunction pf = new PolynomialFunction(solution);
		UnivariateFunction der = pf.derivative();
		return der.value(0.0);
	}

	private double[] solveSystem(double[][] sys, double[] eqs) {
		try {
			LUDecomposition dec = new LUDecomposition(new Array2DRowRealMatrix(sys));
			DecompositionSolver solver = dec.getSolver();
			RealVector vect = solver.solve(new ArrayRealVector(eqs));
			double[] solution = vect.toArray();
			return solution;
		} catch (SingularMatrixException exc) {
			exc.printStackTrace();
			return null;
		}
	}
	private double computeTimePower(int fp, int pow, double period) {
		double t = -left * period;
		double s = 0.0;
		double n = left + 1 + right;
		for (int i=fp-left; i<=fp+right; i++) {
			s += Math.pow(t, pow) / n;
			t += period;
		}
		return s;
	}
	private double computeValueTimePowerSum(double[] data, int fp, int pow, double period) {
		double t = -left * period;
		double s = 0.0;
		double n = left + 1 + right;
		for (int i=fp-left; i<=fp+right; i++) {
			s += Math.pow(t, pow) * data[i] / n;
			t += period;
		}
		return s;
	}
}
