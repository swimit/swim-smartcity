package it.swim.traffic.service;

import swim.api.AbstractService;
import swim.api.JoinValueLane;
import swim.api.MapDownlink;
import swim.api.MapLane;
import swim.api.SpatialLane;
import swim.api.SwimLane;
import recon.Form;
import recon.Value;
import swim.util.HashTrieMap;
import swim.util.Uri;

public class CityService extends AbstractService {
  MapDownlink<Uri, Value> intersectionsLink;


  @SwimLane("intersections")
  public JoinValueLane<Uri, Value> intersections = joinValueLane()
      .keyForm(Form.URI)
      .isResident(true);

  public void linkIntersections() {
    if (intersectionsLink == null) {
      intersectionsLink = downlinkMap()
          .keyForm(Form.URI)
          .hostUri(TRAFFIC_HOST)
          .nodeUri(Uri.EMPTY.withPath(nodeUri().getPath()))
          .laneUri("intersections")
          .didUpdate(this::didUpdateRemoteIntersection)
          .open();
    }
  }

  public void unlinkIntersections() {
    if (intersectionsLink != null) {
      intersectionsLink.close();
      intersectionsLink = null;
    }
  }

  void didUpdateRemoteIntersection(Uri intersectionUri, Value newValue, Value oldValue) {
    //System.out.println(nodeUri().toUri() + " didUpdateRemoteIntersection: " + intersectionUri.toUri());
    if (!intersections.containsKey(intersectionUri)) {
      intersections.downlink(intersectionUri)
          .nodeUri(intersectionUri)
          .laneUri(INTERSECTION_INFO)
          .open();
    }
  }

  public void didStart() {
    System.out.println(nodeUri().toUri() + " didStart");
    linkIntersections();
  }

  public void willStop() {
    unlinkIntersections();
  }


  static final Uri TRAFFIC_HOST = Uri.parse("swims://trafficware.swim.services?key=ab21cfe05ba-7d43-69b2-0aef-94d9d54b6f65");
  static final Uri INTERSECTION_INFO = Uri.parse("intersection/info");
  static final Uri NEIGHBOR_ADD = Uri.parse("neighbor/add");
  static final Uri NEIGHBOR_REMOVE = Uri.parse("neighbor/remove");
}
