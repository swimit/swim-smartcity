package it.swim.traffic;

import swim.api.AbstractPlane;
import swim.api.ServiceType;
import swim.api.SwimRoute;
import swim.api.SwimService;
import recon.Recon;
import recon.Value;
import swim.server.PlaneDef;
import swim.server.ServerDef;
import swim.server.SwimPlane;
import swim.server.SwimServer;
import it.swim.traffic.service.CityService;
import it.swim.traffic.service.IntersectionService;
import swim.util.Decodee;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TrafficPlane extends AbstractPlane {
  @SwimService(name = "city")
  @SwimRoute("/city/:id")
  final ServiceType<?> cityService = serviceClass(CityService.class);

  @SwimService(name = "intersection")
  @SwimRoute("/intersection/:country/:state/:city/:id")
  final ServiceType<?> intersectionService = serviceClass(IntersectionService.class);

  public static void main(String[] args) throws IOException {
    String configPath = null;
    for (int i = 0, n = args.length; i < n; i += 1) {
      final String arg = args[i];
      if (arg.startsWith("-")) {
        // TODO
      } else {
        configPath = arg;
      }
    }
    if (configPath == null) {
      configPath = System.getProperty("swim.config");
      if (configPath == null) {
        configPath = "/swim-smartcity.recon";
      }
    }

    InputStream configInput = null;
    Value configValue;
    try {
      final File configFile = new File(configPath);
      if (configFile.exists()) {
        configInput = new FileInputStream(configFile);
      } else {
        configInput = TrafficPlane.class.getResourceAsStream(configPath);
      }
      configValue = Decodee.readUtf8(Recon.FACTORY.blockParser(), configInput);
    } finally {
      try {
        configInput.close();
      } catch (Exception swallow) {}
    }

    final ServerDef serverDef = ServerDef.FORM.cast(configValue);
    final PlaneDef planeDef = serverDef.getPlaneDef("traffic");

    final SwimServer server = new SwimServer();
    server.materialize(serverDef);
    final SwimPlane planeContext = server.getPlane("traffic");
    final TrafficPlane trafficPlane = (TrafficPlane)planeContext.getPlane();

    server.start();
    planeContext.command("/city/PaloAlto_CA_US", "wake", Value.ABSENT);
    System.out.println("Running TrafficPlane ...");
    server.run(); // blocks until termination
  }
}
