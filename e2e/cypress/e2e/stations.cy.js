describe("Station listing", () => {
  beforeEach(() => {
    cy.visit("/");
    cy.get("[data-cy=navbar-link-stations]").click()
  });

  it("displays some elements", () => {
    cy.get("[data-cy=station-list-table-body]").find("tr").its("length").should("be.gt", 0)
  });

  it("first station link can be clicked and page changes", () => {
    cy.get("[data-cy=station-list-table-body]").find("tr").first().find("a").then(($stationAnchor) => {
      const title = $stationAnchor.text();
      const href = $stationAnchor.attr("href");

      cy.wrap($stationAnchor).click();
      cy.url().should("include", href);
      cy.get("[data-cy=station-names-container]").contains(title);
    });
  });
});
