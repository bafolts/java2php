package translator;

public class Finally extends FileTranslator {

	private BlockBody m_bBlock = null;

	public Finally(String line) {
		setContents(line.trim().substring(3));
		m_bBlock = new BlockBody(getAndSetNextBlock());
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}

}