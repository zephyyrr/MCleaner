import java.io.File;
import java.io.FileFilter;
import java.util.concurrent.ConcurrentLinkedQueue;


public class Main implements Runnable {

	ConcurrentLinkedQueue<File> queue;
	int maxX;
	int maxZ;

	public Main(int maxX, int maxZ, String path) {
		String newPath = "";
		if (path.startsWith("/") || path.startsWith(":\\", 1)) {
			newPath = path;
		} else {
			newPath = System.getProperty("user.dir") +  System.getProperty("file.separator");
			newPath += path;
		}
		//		System.out.println("newPath: " + newPath);

		this.maxX = maxX;
		this.maxZ = maxZ;
		
		queue = new ConcurrentLinkedQueue<File>();

		File root = new File(newPath).getParentFile();
		System.out.println(root.getAbsolutePath());

		File[] files = root.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		});

		if(files == null) {
			System.err.println("FOLDER IS EMPTY");
			System.exit(-4);
		}

		File regions = null;
		for (int i = 0; i < files.length; i++) {
			if (files[i].getName().equals("region")) regions = files[i];
		}

		if (regions == null) {
			System.err.println("ERROR! REGION FOLDER IS NOT PRESENT AT GIVEN LOCATION");
			System.err.println("Could not find " + root.getAbsolutePath() + System.getProperty("file.separator") + "region");
			System.exit(-3);
		}

		files = regions.listFiles(new FileFilter() {

			@Override
			public boolean accept(File pathname) {
				return pathname.getName().matches("r\\.(-|)[0-9]*\\.(-|)[0-9]*\\.mcr");
			}
		});


		for (int i = 0; i < files.length; i++) {
			queue.add(files[i]);
		}
		
		Thread[] threads = new Thread[(files.length/2 < 20) ? files.length/2 : 20];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(this);
			threads[i].start();
		}
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			Main.showHelp();
			System.exit(-1);
		}

		int maxX = 0;
		int maxZ = 0;

		try {
			maxX = Integer.parseInt(args[0]);
			maxZ = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			Main.showHelp();
			System.exit(-2);
		}

		new Main(maxX, maxZ, args[2]);
	}

	private static void showHelp() {
		// TODO Auto-generated method stub
		System.out.println("USAGE:");
		System.out.println("java -jar MCleaner.jar <max X> <max Z> <Path to level.dat>");
	}

	public void run() {
		String name = "";
		int x = 0;
		int z = 0;

		while(true) {
			
			File file = queue.poll();
			if(file == null) {
				break;
			}

			name = file.getName();
			String[] splitname = name.split("\\.");

			try {
				x = Math.abs(Integer.parseInt(splitname[1]));
				z = Math.abs(Integer.parseInt(splitname[2]));

			} catch (NumberFormatException e) {
				continue;
			}

			if (x > maxX || z > maxZ) {
				System.out.println("Removed: " + name);
				file.delete();
			}

			name = "";
			x = 0;
			z = 0;
		}
	}

}
