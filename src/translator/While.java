package translator;

public class While extends FileTranslator {

	private Argument m_aArgument = null;
	private BlockBody m_bBlock = null;

	public While(String line) {
		setContents(line.trim().substring(5));
		String sArgument = getAndSetNextParen().trim();
		sArgument = sArgument.substring(1,sArgument.length()-1);
		m_aArgument = new Argument(sArgument);

	}

	public Argument getArgument() {
		return m_aArgument;
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}
	
	public void setBlockBody(BlockBody b) {
		m_bBlock = b;
	}

}