package com.seven.asimov.it.rest.resource.cms;

import com.seven.asimov.it.rest.model.ConfigurationNode;
import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.annotations.providers.multipart.PartType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
public interface ConfigurationResource {

    @POST
    @Path("/")
    public void importConfiguration(ConfigurationNode node);

    @POST
    @Path("/global")
    public void importGlobalConfiguration(ConfigurationNode node);

    @GET
    @Path("/")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public ConfigurationNode exportConfiguration();

    @GET
    @Path("/global")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    public ConfigurationNode exportGlobalConfiguration();

    @POST
    @Path("/overwrite")
    public void replaceConfiguration(ConfigurationNode node);

    @POST
    @Path("/file")
    @Consumes("multipart/form-data")
    public void importConfigurationFromFile(@MultipartForm ConfigurationFile configuration);

    @POST
    @Path("/global/file")
    @Consumes("multipart/form-data")
    public void importGlobalConfigurationFromFile(@MultipartForm ConfigurationFile configuration);

    @POST
    @Path("/file/overwrite")
    @Consumes("multipart/form-data")
    public void replaceConfiguration(@MultipartForm ConfigurationFile configuration);

    @GET
    @Path("/file")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportConfigurationToFile();

    @GET
    @Path("/global/file")
    @Produces({MediaType.APPLICATION_JSON})
    public Response exportGlobalConfigurationToFile();

    public class ConfigurationFile {

        public ConfigurationFile() {
        }

        private byte[] data;

        public byte[] getData() {
            return data;
        }

        @FormParam("configurationFile")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public void setData(byte[] data) {
            this.data = data;
        }
    }
}
