package translator;

public class Synchronized extends FileTranslator {

	private BlockBody m_bBlockBody = null;

	public Synchronized(String line) {
		setContents(line.trim());
		setBlockBody(new BlockBody(getAndSetNextBlock()));
	}
	
	public void setBlockBody(BlockBody b) {
		m_bBlockBody = b;
	}
	
	public BlockBody getBlockBody() {
		return m_bBlockBody;
	}

}