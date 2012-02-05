
public class Utility {

	public static <E> E getRandomArrayElement(E[] array) {
		return array[(int) (Math.random() * array.length)];
	}

	public static int getRandomInRange(int low, int high) {
		return (int) ((Math.random() * (high - low + 1)) + low);
	}

	public static boolean within(int target, int range, int value) {
		return value >= target - range && value <= target + range;
	}

}
