package translator;

public class Else extends FileTranslator {

	private BlockBody m_bBlock = null;

	public Else() {
	
	}

	public Else(String line) {
		line = line.trim();
		//setPosition(getPosition()+4);
		setContents(line);
		m_bBlock = new BlockBody(getAndSetNextBlock());
	}

	public BlockBody getBlockBody() {
		return m_bBlock;
	}

	public void setBlockBody(BlockBody b) {
		m_bBlock = b;
	}

}