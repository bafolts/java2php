import javax.servlet.http;

/**
 * Represents a service for the classic hello world program.
 */
public class HelloWorldServlet extends javax.servlet.http.HttpServlet {

	/**
	 *	The GET Handler. This will output the classic message.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		resp.getWriter().print("Hello World!");
	}

}