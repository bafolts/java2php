package translator;

public class Catch extends FileTranslator {

	private Parameter m_aParameter = null;
	private BlockBody m_bBlock = null;

	public Catch(String line) {
		line = line.trim();
		setContents(line);
		setPosition(getPosition()+5);

		String sParam = getAndSetNextParen().trim();
		sParam = sParam.substring(1,sParam.length()-1);

		m_aParameter = new Parameter(sParam);

		m_bBlock = new BlockBody(getAndSetNextBlock());
	}
	
	public Parameter getParameter() {
		return m_aParameter;
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}

}