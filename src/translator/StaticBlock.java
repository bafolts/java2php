package translator;

public class StaticBlock {

	private BlockBody m_bBlock = null;

	public StaticBlock(String line) {
		line = line.trim();
		m_bBlock = new BlockBody(line);
	}
	
	public BlockBody getBlockBody() {
		return m_bBlock;
	}
	
	public void setBlockBody(BlockBody b) {
		m_bBlock = b;
	}

}