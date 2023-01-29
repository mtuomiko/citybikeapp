describe("Station listing", () => {
  beforeEach(() => {
    cy.visit("/");
    cy.get("[data-cy=navbar-link-stations]").click()
  });

  it("displays some elements", () => {
    cy.get("[data-cy=station-list-table-body]").find("tr").its("length").should("be.gt", 0)
  });

  it("first station link can be clicked", () => {
    cy.get("[data-cy=station-list-table-body]").find("tr").first().find("a").click()
  });
});
