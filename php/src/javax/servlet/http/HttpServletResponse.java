package PLACEHOLDERS;

public interface HttpServletResponse {
	java.io.PrintWriter getWriter() throws java.io.IOException;
	void setContentType(java.lang.String type);
	void sendRedirect(java.lang.String location) throws java.io.IOException;
}