package uk.gov.ons.census.fwmt.jobservice.config;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import uk.gov.census.ffa.storage.utils.StorageUtils;

@SuppressFBWarnings(value="DM_EXIT", justification="App shouldnt start up")
@Configuration
public class GpgConfig {

  @Value("${decryption.pgp}")
  private String privateKeyLocation;
  
  @Autowired
  private StorageUtils storageUtils;
  
  @Bean
  public byte[] privateKeyByteArray() throws IOException{
    try {
      URI privateKeyUri = URI.create(privateKeyLocation);
      InputStream fileInputStream = storageUtils.getFileInputStream(privateKeyUri);
      byte[] readAllBytes;
      readAllBytes = fileInputStream.readAllBytes();
      return readAllBytes;
    } catch (IOException e) {
      System.exit(128);
      throw e;
    }
  }

}
