describe("Journey listing", () => {
  beforeEach(() => {
    cy.visit("/")
    cy.get("[data-cy=navbar-link-journeys]").click()
  });

  it("displays some elements", () => {
    cy.get("[data-cy=journey-list-table-body]").find("tr").its("length").should("be.gt", 0)
  });

  it("first journey's departure station link can be clicked and URL changes", () => {
    cy.get("[data-cy=journey-list-table-body]").find("tr").first().find("a").first().then(($departureAnchor) => {
      const title = $departureAnchor.text();
      const href = $departureAnchor.attr("href");

      cy.wrap($departureAnchor).click();
      cy.url().should("include", href);
      cy.get("[data-cy=station-names-container]").contains(title);
    });
  });

  it("first journey's arrival station link can be clicked and URL changes", () => {
    cy.get("[data-cy=journey-list-table-body]").find("tr").first().find("a").last().then(($arrivalAnchor) => {
      const title = $arrivalAnchor.text();
      const href = $arrivalAnchor.attr("href");

      cy.wrap($arrivalAnchor).click();
      cy.url().should("include", href);
      cy.get("[data-cy=station-names-container]").contains(title);
    });
  });
});
