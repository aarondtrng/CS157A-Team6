package servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.ImageSniffer;
import util.ImageStore;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Serves uploaded item photos at /uploads/<name>. Public, like the posts they belong to. Only the exact
 * generated file names are served (see ImageStore.resolve), never an arbitrary path.
 */
@WebServlet("/uploads/*")
public class UploadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        String name = pathInfo == null || pathInfo.length() < 2 ? null : pathInfo.substring(1);
        Path file = ImageStore.resolve(name);
        if (file == null || !Files.isRegularFile(file)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        String extension = name.substring(name.lastIndexOf('.') + 1);
        response.setContentType(ImageSniffer.contentTypeFor(extension));
        response.setContentLengthLong(Files.size(file));
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Cache-Control", "public, max-age=86400");
        Files.copy(file, response.getOutputStream());
    }
}
