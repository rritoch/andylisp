/**
 *   Copyright (c) Ralph Ritoch. All rights reserved.
 *   The use and distribution terms for this software are covered by the
 *   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 *   which can be found in the file epl-v10.html at the root of this distribution.
 *   By using this software in any fashion, you are agreeing to be bound by
 * 	 the terms of this license.
 *   You must not remove this notice, or any other, from this software.
 **/

package clojure.lang;

public interface INumber {

	public boolean isZero();

	public boolean isPos();

	public boolean isNeg();

	public Number add(Number y);

	public Number addP(Number y);

	public Number multiply(Number y);

	public Number multiplyP(Number y);

	public Number divide(Number y);

	public Number quotient(Number y);

	public Number remainder(Number y);

	public boolean equiv(Number y);

	public boolean lt(Number y);

	public boolean gt(Number y);

	public boolean lte(Number y);

	public boolean gte(Number y);

	public Number subtract(Number y);

	public Number subtractP(Number y);

	public Number negate();

	public Number negateP();

	public Number inc();

	public Number incP();

	public Number dec();

	public Number decP();
}

