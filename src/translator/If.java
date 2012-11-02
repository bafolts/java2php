package translator;

public class If extends FileTranslator {

	private Argument m_aArgument = null;
	private BlockBody m_bBlock = null;

	public If(String line) {
		line = line.trim();
		setContents(line);
		setPosition(getPosition()+2);

		String s = getAndSetNextParen().trim();
		s = s.substring(1,s.length()-1);
		m_aArgument = new Argument(s);

	}

	public void setBlockBody(BlockBody b) {
		m_bBlock = b;
	}

	public Argument getArgument() {
		return m_aArgument;
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}

}